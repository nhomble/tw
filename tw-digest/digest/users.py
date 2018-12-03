import argparse
import csv

import digest


def main(host: str, port: int, db_name: str, target: str):
    store = digest.TwitchDataStore.connect_to(host, port, db_name, direction="none", limit=3000)

    with open(digest.off_root(target, f="user.csv"), 'w') as user_file:
        csvfile = csv.DictWriter(user_file, delimiter=',', fieldnames=store.user_headers)
        csvfile.writeheader()
        for row in store.users_collection.find({"$and": [{"_complete": False}, ]}).sort("totalFollowers"):
            print(row["name"])


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("-o", "--out", default=digest.TARGET_DIR)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.out)
