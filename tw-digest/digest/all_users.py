import argparse

import digest


def main(host: str, port: int, db_name: str, target: str):
    store = digest.TwitchDataStore.connect_to(host, port, db_name, direction="none", limit=3000)

    with open(digest.off_root(target, f="user_list.csv"), 'w') as f:
        i = 0
        for user in store.users_collection.distinct("name"):
            if i % 100 == 0:
                print("i={} user={}".format(i, user))
            i += 1
            f.write("{}\n".format(user))


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("-o", "--out", default=digest.TARGET_DIR)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.out)
