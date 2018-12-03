import math
import argparse

import numpy as np
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt

import digest


def main(host: str, port: int, db_name: str, factor: str):
    buckets = 75
    store = digest.TwitchDataStore.connect_to(host, port, db_name)
    x = [doc["totalGamesBroadcasted"] for doc in store.users_collection.find(digest.QUERY_COMPLETE_BY(factor, x=1000000))]

    n, bins, patches = plt.hist(x, buckets, facecolor='green')

    plt.xlabel("bins({})".format(buckets))
    # plt.yscale('log', nonposy='clip')
    plt.ylabel("log({})".format(factor))
    plt.title("Histogram of {} on a log scale".format(factor))
    plt.grid(True)

    plt.show()


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("--factor", default="totalFollowers")
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.factor)
