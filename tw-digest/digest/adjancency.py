"""
All we really want to do here is create some csv for more analysis
"""

import argparse
import csv

import digest


def main(host: str, port: int, db_name: str, target: str):
    store = digest.TwitchDataStore.connect_to(host, port, db_name, limit=5000, direction=digest.DESCENDING)

    with open(digest.off_root(target, f="games.csv"), 'w') as game_file:
        csvfile = csv.DictWriter(game_file, delimiter=',', fieldnames=store.game_headers)
        csvfile.writeheader()
        for row in store.game_rows():
            csvfile.writerow(row)

    with open(digest.off_root(target, f="user_following.csv"), 'w') as uadj_file:
        csvfile = csv.DictWriter(uadj_file, delimiter=',', fieldnames=store.f_headers)
        csvfile.writeheader()
        i = 0
        for row in store.user_following():
            if i % 100 == 0:
                print("user_following i={}".format(i))
            i += 1
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

    with open(digest.off_root(target, f="user_genres.csv"), 'w') as ug_file:
        csvfile = csv.DictWriter(ug_file, delimiter=',', fieldnames=store.user_genre_headers)
        csvfile.writeheader()
        for row in store.user_genre():
            csvfile.writerow(row)

    with open(digest.off_root(target, f="user_unweighted_summary.csv"), 'w') as us_file:
        csvfile = csv.DictWriter(us_file, delimiter=',', fieldnames=store.user_summary_headers)
        csvfile.writeheader()
        i = 0
        for row in store.user_summary():
            if i % 100 == 0:
                print("user_unweighted_summary={}".format(i))
            i += 1
            csvfile.writerow(row)


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("-o", "--out", default=digest.TARGET_DIR)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.out)
