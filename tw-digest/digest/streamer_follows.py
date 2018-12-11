import argparse
import csv

import digest


def main(host: str, port: int, db_name: str, target: str):
    store = digest.TwitchDataStore.connect_to(host, port, db_name, direction="none", limit=3000)
    all_games = digest.all_known_games()
    with open("resources/follows.csv", 'r') as f:
        line = f.readline().split(",")
        users = line[1:]

    with open(digest.off_root(target, f="following_streamer_game_data.csv"), 'w') as f:
        csvfile = csv.DictWriter(f, fieldnames=["user"] + all_games)
        csvfile.writeheader()
        for user in users:
            doc = store.users_collection.find_one({"name": user})

            if doc is None:
                player_games = []
            else:
                player_games = [obj.get("game", "") for obj in doc["gameBroadcasts"]]
            d = {g: 1 if g in player_games else 0 for g in all_games}
            d["user"] = user
            csvfile.writerow(d)


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("-o", "--out", default=digest.TARGET_DIR)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.out)
