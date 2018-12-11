import argparse
import csv

import pymongo

import digest


def main(host: str, port: int, db_name: str, target: str):
    n = 2000
    store = digest.TwitchDataStore.connect_to(host, port, db_name, limit=n, direction=digest.DESCENDING)
    followers = store.users_collection.find({"totalGamesBroadcasted": {"$gte": 3}}).sort("totalFollower", pymongo.ASCENDING).distinct("name")
    followers = list(followers)[:n]

    source = store.links_collection.find({"follower": {"$in": followers}}).distinct("sourceStreamer")
    dest = store.links_collection.find({"follower": {"$in": followers}}).distinct("linkedStreamer")
    all_users = list(set(source + dest))
    i = 0
    with open(digest.off_root(target, f="linkage_users_redux.csv"), 'w') as f:
        csvfile = csv.DictWriter(f, delimiter=',', fieldnames=["user"] + all_users)
        csvfile.writeheader()
        for name in all_users:
            if i % 20 == 0:
                print("i={} name={}".format(i, name))
            i += 1
            linked_to = [doc["linkedStreamer"] for doc in store.links_collection.find({"sourceStreamer": name})]
            linked_from = [doc["sourceStreamer"] for doc in store.links_collection.find({"linkedStreamer": name})]
            row = {other: 1 if other in linked_to or other in linked_from else 0 for other in all_users}
            row["user"] = name
            csvfile.writerow(row)
    all_games = digest.all_known_games()

    with open(digest.off_root(target, f="linkage_redux_attr.csv"), 'w') as f:
        csvfile = csv.DictWriter(f, delimiter=',',
                                 fieldnames=["user", "game", "genre", "hundreds", "thousands", "millions", "followers",
                                             "following"] + all_games)
        csvfile.writeheader()
        for user in all_users:
            doc = store.users_collection.find_one({"name": user})
            game_names = []
            try:
                game_names = [ele.get("game") for ele in doc["gameBroadcasts"] if "game" in ele]
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
            if doc is None:
                print(user)
            count = 0
            d = {
                "user": user,
                "game": ascii(game),
                "genre": genre,
                "hundreds": count > 100,
                "thousands": count > 10000,
                "millions": count > 1000000,
                "followers": doc["totalFollowers"],
                "following": doc["totalFollowing"]
            }
            for g in all_games:
                if g == game:
                    d[g] = 1
                else:
                    d[g] = 0
            csvfile.writerow(d)


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("-o", "--out", default=digest.TARGET_DIR)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.out)
