import argparse
import csv

import digest


def main(host: str, port: int, db_name: str, target: str):
    all_games = digest.all_known_games()
    n = 1000
    store = digest.TwitchDataStore.connect_to(host, port, db_name, limit=n, direction=digest.DESCENDING)

    sources = [user for user in store.users if
               store.links_collection.find_one({"$or": [{"sourceStreamer": user}, {"linkedStreamer": user}]})]

    destinations = store.links_collection.find().distinct("sourceStreamer")
    destinations = [ele for ele in destinations if store.users_collection.find_one(
        {"$and": [{"name": ele}, {"totalGameBroadcasts": {"$gte": 1}}]}) is not None and
                    store.links_collection.find_one()]
    destinations = list(destinations)[:n]
    nested = []
    for linked in destinations:
        nested.append(store.links_collection.find({"sourceStreamer": linked}).distinct("sourceStreamer"))
    more_streamers = [s for sublist in nested for s in sublist]
    more_streamers = list(set([s for s in more_streamers if store.users_collection.find_one({"name": s}) is not None]))

    sources = [ele for ele in sources if store.users_collection.find_one({"name": ele}) is not None]

    headers = list(set(sources + destinations + more_streamers))
    print("headers={} length={}".format(headers, len(headers)))
    i = 0
    with open(digest.off_root(target, f="follows.csv"), 'w') as f:
        csvfile = csv.DictWriter(f, delimiter=',', fieldnames=["user"] + headers)
        csvfile.writeheader()
        for name in headers:
            if i % 20 == 0:
                print("i={} name={}".format(i, name))
            i += 1
            linked_to = [doc["linkedStreamer"] for doc in store.links_collection.find({"sourceStreamer": name})]
            linked_from = [doc["sourceStreamer"] for doc in store.links_collection.find({"linkedStreamer": name})]
            row = {other: 1 if other in linked_to or other in linked_from else 0 for other in headers}
            row["user"] = name
            csvfile.writerow(row)

    with open(digest.off_root(target, f="follows_user_summary.csv"), 'w') as f:
        csvfile = csv.DictWriter(f, delimiter=',',
                                 fieldnames=["user", "game", "genre", "hundreds", "thousands", "millions", "followers",
                                             "following"] + all_games)
        csvfile.writeheader()
        for user in headers:
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
