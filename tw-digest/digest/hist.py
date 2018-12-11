import math
import argparse

import numpy as np
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt

import digest


def main(host: str, port: int, db_name: str, factor: str):

    store = digest.TwitchDataStore.connect_to(host, port, db_name)
    M = 2000
    buckets = 50

    x = [(doc["totalFollowing"] + 1) / (doc["totalFollowers"] + 1 ) for doc in store.users_collection.find({"totalFollowing": {"$lte": M}})]

    print("COUNT={}".format(store.users_collection.find({"totalFollowing": {"$gt": M}}).count()))
    plt.hist(x, buckets, facecolor='green')

    plt.ylabel("Frequency of users")
    plt.xlabel("Buckets={} by proportion (out-degree + 1) / (in-degree + 1)".format(buckets))
    plt.title("Frequency of users by the proportion of their out-degree : in-degree")
    plt.show()


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("--factor", default="totalFollowers")
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.factor)
