import argparse
import csv

import digest


def main(host: str, port: int, db_name: str, target: str):
    n = 5000
    store = digest.TwitchDataStore.connect_to(host, port, db_name, limit=n, direction=digest.DESCENDING)

    followers = store.links_collection.find().limit(n).distinct("follower")
    q = {"follower": {"$in": followers}}
    sources = store.links_collection.find(q).distinct("sourceStreamer")
    destinations = store.links_collection.find(q).distinct("linkedStreamer")
    headers = list(set(sources + destinations))[:n]
    print("headers length={}".format(len(headers)))
    i = 0
    with open(digest.off_root(target, f="follows.csv"), 'w') as f:
        csvfile = csv.DictWriter(f, delimiter=',', fieldnames=headers)
        for name in headers:
            if i % 20 == 0:
                print("i={} name={}".format(i, name))
            i += 1
            linked_to = [doc["linkedStreamer"] for doc in store.links_collection.find({"sourceStreamer": name})]
            row = {other: 1 if other in linked_to else 0 for other in headers}
            csvfile.writerow(row)

    with open(digest.off_root(target, f="follows_user_summary.csv"), 'w') as f:
        csvfile = csv.DictWriter(f, delimiter=',', fieldnames=["user", "game", "genre"])
        for user in headers:
            doc = store.users_collection.find_one({"name": user})
            game_names = []
            try:
                game_names = [ele.get("game") for ele in doc["gamesBroadcasted"] if "game" in ele]
            except Exception as e:
                print(e)
            game = ""
            if len(game_names) > 0:
                game = digest.most_frequent(game_names)
            print("game={}".format(game))
            nested = [store.games_collection.find_one({"$and": [{"game": game}, {"genres": {"$exists": True}}]}) for
                      game
                      in game_names]
            nested = [doc["genres"] for doc in nested if doc is not None]
            genres = [genre for sublist in nested if len(sublist) > 0 for genre in sublist]
            genre = ""
            if len(genres) > 0:
                genre = digest.most_frequent(genres)
            d = {
                "user": user,
                "game": ascii(game),
                "genre": genre
            }
            csvfile.writerow(d)


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("-o", "--out", default=digest.TARGET_DIR)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.out)
