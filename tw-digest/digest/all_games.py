import argparse

import digest


def main(host: str, port: int, db_name: str, target: str):
    store = digest.TwitchDataStore.connect_to(host, port, db_name, direction="none", limit=3000)
    games = set()
    i = 0
    for doc in store.users_collection.find():
        i += 1
        if i % 5000 == 0:
            print("i={} doc={}".format(i, doc))
        for broadcast in doc["gameBroadcasts"]:
            ele = broadcast.get("game", "CUSTOM")
            if ele == '':
                ele = "CUSTOM"
            games.add(ele)

    with open(digest.off_root(target, f="game_list.csv"), 'w') as f:
        for game in games:
            f.write("{}\n".format(ascii(game)))


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("-o", "--out", default=digest.TARGET_DIR)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.out)
