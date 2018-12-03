"""
All we really want to do here is create some csv for more analysis
"""

import argparse
import pymongo as mongo
import csv

import digest

_user_collection = "users"
_game_collection = "games"


class TwitchDataStore:
    def __init__(self, client, db_name):
        self._client = client
        self._db_name = db_name
        self._users = self.users_collection.distinct("name")
        self._games = self.games_collection.distinct("game")

    @property
    def db(self):
        return self._client[self._db_name]

    @staticmethod
    def connect_to(host: str, port: int, name: str):
        client = mongo.MongoClient(host=host, port=port)
        return TwitchDataStore(client, name)

    @property
    def user_headers(self):
        return ["createdAt", "name", "totalFollowers", "totalFollowing", "totalGamesBroadcasted"]

    @property
    def game_headers(self):
        return ["viewers", "game", "popularity"]

    @property
    def games_collection(self):
        return self.db[_game_collection]

    @property
    def users_collection(self):
        return self.db[_user_collection]

    @property
    def users(self):
        return self._users

    @property
    def games(self):
        return self._games

    def _get_users(self):
        return self.users_collection.find({"_complete": True})

    def user_rows(self):
        for doc in self._get_users():
            yield {k: v for (k, v) in doc.items() if k in self.user_headers}

    def game_rows(self):
        for doc in self.games_collection.find():
            yield {k: ascii(v) for (k, v) in doc.items() if k in self.game_headers}

    def user_followers(self):
        for doc in self._get_users():
            yield {name: 1 if name in doc["followers"] else 0 for name in self._users}

    def user_following(self):
        for doc in self._get_users():
            yield {name: 1 if name in doc["following"] else 0 for name in self._users}

    def user_playing(self):
        for doc in self._get_users():
            playing = [o.get("game", "") for o in doc["gamesBroadcasted"]]
            d = {ascii(g): 1 if g in playing else 0 for g in self.games}
            d["user"] = doc["name"]
            yield d

    @property
    def user_playing_headers(self):
        return ["user"] + [ascii(w) for w in self.games]


def main(host: str, port: int, db_name: str, target: str):
    store = TwitchDataStore.connect_to(host, port, db_name)

    with open(digest.off_root("target", f="user.csv"), 'w') as user_file:
        csvfile = csv.DictWriter(user_file, delimiter=',', fieldnames=store.user_headers)
        csvfile.writeheader()
        for row in store.user_rows():
            csvfile.writerow(row)

    with open(digest.off_root("target", f="games.csv"), 'w') as game_file:
        csvfile = csv.DictWriter(game_file, delimiter=',', fieldnames=store.game_headers)
        csvfile.writeheader()
        for row in store.game_rows():
            csvfile.writerow(row)

    with open(digest.off_root("target", f="user_following.csv"), 'w') as uadj_file:
        csvfile = csv.DictWriter(uadj_file, delimiter=',', fieldnames=store.users)
        csvfile.writeheader()
        for row in store.user_following():
            csvfile.writerow(row)

    with open(digest.off_root("target", f="user_followers.csv"), 'w') as uadj_file:
        csvfile = csv.DictWriter(uadj_file, delimiter=',', fieldnames=store.users)
        csvfile.writeheader()
        for row in store.user_followers():
            csvfile.writerow(row)

    with open(digest.off_root("target", f="user_games.csv"), 'w') as gadj_file:
        csvfile = csv.DictWriter(gadj_file, delimiter=',', fieldnames=store.user_playing_headers)
        csvfile.writeheader()
        for row in store.user_playing():
            csvfile.writerow(row)


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _parser.add_argument("-o", "--out", default=digest.target_dir)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db, _args.out)
