import argparse
import csv
import datetime

import digest


def main(host: str, port: int, db_name: str):
    today = datetime.datetime.today()
    last_month = today - datetime.timedelta(days=30)
    store = digest.TwitchDataStore.connect_to(host, port, db_name)
    games = []
    for doc in store.games_collection.find({"popularity": {"$exists": True}}).sort("popularity", digest.DESCENDING):
        item = {"game": doc["game"], "popularity": doc["popularity"], "users": [], "viewers": doc["viewers"]}
        for user in store.users_collection.find({
            "$and": [
                digest.QUERY_COMPLETE_BY("totalFollowers", x=10000),
                {"gamesBroadcasted": {"$elemMatch": {"game": doc["game"]}}}
            ]
        }):
            broadcasts = [ele for ele in user["gamesBroadcasted"] if ele["createdAt"] > last_month]
            if doc["game"] in [b["game"] for b in broadcasts]:
                item["users"].append(user)
        games.append(item)

    with open(digest.off_root("target", f="relevancy.csv"), 'w') as f:
        c = csv.writer(f)
        for i in games:
            if len(i["users"]) > 0:
                print(i["game"])
            c.writerow([i["popularity"], len(i["users"])])


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db)
