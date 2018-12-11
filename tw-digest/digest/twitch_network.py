import argparse
import csv

import pymongo

import digest


def read_list(target, rf):
    all_ele = []
    with open(digest.off_root(target, f=rf), 'r') as f:
        for line in f.readlines():
            all_ele.append(line.strip().replace("'", ""))
    return all_ele


def main(host: str, port: int, db_name: str, target: str):
    store = digest.TwitchDataStore.connect_to(host, port, db_name, direction="none", limit=3000)
    n = 500

    def game_name(check: str, broadcasts: iter):
        return any([check in b.lower() for b in broadcasts])

    all_games = {
        "fortnite": [
            lambda broadcasts: game_name("fortnite", broadcasts)
        ],
        "darksouls": [
            lambda broadcasts: game_name("darksoul", broadcasts)
        ],
        "dota": [
            lambda broadcasts: game_name("dota", broadcasts)
        ],
        "lol": [
            lambda broadcasts: game_name("league of legend", broadcasts),
            lambda broadcasts: game_name("lol", broadcasts)
        ],
        "cod": [
            lambda broadcasts: game_name("call of duty", broadcasts),
            lambda broadcasts: game_name("black ops", broadcasts)
        ],
        "fifa": [
            lambda broadcasts: game_name("fifa", broadcasts)
        ],
        "cs": [
            lambda broadcasts: game_name("counter strike", broadcasts),
            lambda broadcasts: game_name("counter-strike", broadcasts)
        ],
        "diablo": [
            lambda broadcasts: game_name("diablo", broadcasts)
        ],
        "sc": [
            lambda b: game_name("sc2", b),
            lambda b: game_name("starcraft", b)
        ],
        "warcraft": [
            lambda b: game_name("warcraft", b)
        ]
    }

    game_users = {
        "fortnite_c": ["fortnite"],
        "riot": ["riotgames", "lolesportslas", "lck1", "els_lol"],
        "pubg": ["esl_pubg"],
        "starcraft": ["starcraft", "gsl"],
        "dota_c": ["dota2ruhub", "beyondthesummit2", "beyondthesummit", "beyondthesummit3"],
        "bethesda": ["bethesda"],
        "csgo": ["esl_csgo", ]
    }

    # linked = store.links_collection.distinct("linkedStreamer")
    # source = store.links_collection.find({"sourceStreamer": {"$in": linked}}).distinct("sourceStreamer")
    # all_users = list(source)[:n]
    all_users = []
    for user_doc in store.users_collection.find(
            #        {"$and": [{"totalGamesBroadcasted": {"$gte": 3}}, {"totalFollowing": {"$gte": 300}}]}
    ).sort("totalFollowers", pymongo.DESCENDING):
        all_users.append(user_doc["name"])
    all_users = all_users[:n]

    mode = "companies_popular_v2"
    with open(digest.off_root(target, f="twitch_network_{}.csv".format(mode)), 'w') as f:
        with open(digest.off_root(target, f="twitch_network_attr_{}.csv".format(mode)), 'w') as attr:
            attr_file = csv.DictWriter(attr, fieldnames=["user"] + list(all_games.keys()) + list(game_users.keys()))
            adj_file = csv.DictWriter(f, fieldnames=["user"] + all_users)

            attr_file.writeheader()
            adj_file.writeheader()

            i = 0
            for user in all_users:
                if i % 100 == 0:
                    print("i={}/{} user={}".format(i, len(all_users), user))
                i += 1
                row = {ele: 0 for ele in all_users}
                row["user"] = user
                for doc in store.links_collection.find(
                        {"$and": [{"sourceStreamer": user}, {"linkedStreamer": {"$in": all_users}}]}):
                    link = doc["linkedStreamer"]
                    row[link] = 1
                # for doc in store.links_collection.find(
                #         {"$and": [{"linkedStreamer": user}, {"sourceStreamer": {"$in": all_users}}]}):
                #     link = doc["sourceStreamer"]
                #     row[link] = 1
                adj_file.writerow(row)

                user_doc = store.users_collection.find_one({"name": user})
                broadcasts = set(ascii(ele.get("game", "CUSTOM")) for ele in user_doc.get("gameBroadcasts", []))
                attr_row = {game: 1 if any([match(broadcasts) for match in matches]) else 0 for (game, matches) in
                            all_games.items()}
                attr_row["user"] = user
                for (guser, arr) in game_users.items():
                    if any([store.links_collection.find_one(
                            {"$and": [{"sourceStreamer": user}, {"linkedStreamer": follow}]}) for follow in arr]):
                        attr_row[guser] = 1
                    else:
                        attr_row[guser] = 0
                attr_file.writerow(attr_row)


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("-o", "--out", default=digest.TARGET_DIR)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.out)
