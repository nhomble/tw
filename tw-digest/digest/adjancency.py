"""
All we really want to do here is create some csv for more analysis
"""

import argparse
import csv

import digest


def main(host: str, port: int, db_name: str, target: str):
    store = digest.TwitchDataStore.connect_to(host, port, db_name)

    with open(digest.off_root(target, f="games.csv"), 'w') as game_file:
        csvfile = csv.DictWriter(game_file, delimiter=',', fieldnames=store.game_headers)
        csvfile.writeheader()
        for row in store.game_rows():
            csvfile.writerow(row)

    with open(digest.off_root(target, f="user_following.csv"), 'w') as uadj_file:
        csvfile = csv.DictWriter(uadj_file, delimiter=',', fieldnames=store.f_headers)
        csvfile.writeheader()
        for row in store.user_following():
            csvfile.writerow(row)

    with open(digest.off_root(target, f="user_followers.csv"), 'w') as uadj_file:
        csvfile = csv.DictWriter(uadj_file, delimiter=',', fieldnames=store.f_headers)
        csvfile.writeheader()
        for row in store.user_followers():
            csvfile.writerow(row)

    with open(digest.off_root(target, f="user_bi.csv"), 'w') as bf:
        csvfile = csv.DictWriter(bf, delimiter=',', fieldnames=store.f_headers)
        csvfile.writeheader()
        for row in store.user_bidirectional():
            csvfile.writerow(row)

    with open(digest.off_root(target, f="user_games.csv"), 'w') as gadj_file:
        csvfile = csv.DictWriter(gadj_file, delimiter=',', fieldnames=store.user_playing_headers)
        csvfile.writeheader()
        for row in store.user_playing():
            csvfile.writerow(row)


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("-o", "--out", default=digest.TARGET_DIR)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.out)
