import math
import argparse

import digest

import matplotlib.pyplot as plt


def main(host, port, db_name):
    plt.close('all')
    store = digest.TwitchDataStore.connect_to(host, port, db_name)
    following = []
    followers = []
    x = range(10)
    for zeros in x:
        less_than = math.pow(10, zeros)
        following.append(store.users_collection.find({"totalFollowing": {"$lte": less_than}}).count())
        followers.append(store.users_collection.find({"totalFollowers": {"$lte": less_than}}).count())

    f, axarr = plt.subplots(2, sharex=True)
    axarr[0].set_title("number of users versus number of other users they follow")
    axarr[0].plot(x, following)

    axarr[1].set_title("number of users versus number of other users that follow them")
    axarr[1].plot(x, followers)



    plt.show()

if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db)
