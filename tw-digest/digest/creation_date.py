import argparse
import matplotlib.pyplot as plt
import datetime
import calendar

import digest


def monthdelta(d1, d2):
    delta = 0
    while True:
        mdays = calendar.monthrange(d1.year, d1.month)[1]
        d1 += datetime.timedelta(days=mdays)
        if d1 <= d2:
            delta += 1
        else:
            break
    return delta


def main(host: str, port: int, db_name: str, factor: str):
    store = digest.TwitchDataStore.connect_to(host, port, db_name)
    dates = [doc["createdAt"] for doc in store.users_collection.find()]
    mn = min(dates)
    mx = max(dates)
    diffs = [monthdelta(mn, m) for m in dates]

    buckets = 50
    plt.hist(diffs, buckets, facecolor='green')
    plt.ylabel("number of users")
    plt.xlabel("number of months since {}".format(mn))
    plt.title("number of user vs the date they created their account")
    plt.show()


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("--factor", default="totalFollowers")
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.factor)
