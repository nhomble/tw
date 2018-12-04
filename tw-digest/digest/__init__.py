import pymongo as mongo
import os

import digest


def off_root(path: str, f=None):
    parts = os.path.split(__file__)
    root = os.path.join(parts[0], path)

    if not os.path.exists(root):
        os.makedirs(root)

    if f is None:
        return root
    else:
        return os.path.join(root, f)


TARGET_DIR = "target"
ASCENDING = mongo.ASCENDING
DESCENDING = mongo.DESCENDING
SPLIT = "split"

_user_collection = "users"
_game_collection = "games"


class TwitchDataStore:
    def __init__(self, client, db_name, limit=2000, direction=digest.DESCENDING):
        self._client = client
        self._db_name = db_name
        self._limit = limit
        self._users = [doc["name"] for doc in self._get_users(direction=direction)]
        self._games = self.games_collection.distinct("game")
        self._genres = [ele for sublist in
                        [doc["genres"] for doc in self.games_collection.find({"genres": {"$exists": True}})] for ele in
                        sublist]
        self._direction = direction

    @property
    def db(self):
        return self._client[self._db_name]

    @staticmethod
    def connect_to(host: str, port: int, name: str, **kwargs):
        client = mongo.MongoClient(host=host, port=port)
        return TwitchDataStore(client, name, **kwargs)

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

    @property
    def genres(self):
        return self._genres

    @property
    def f_headers(self):
        return ["user"] + self._users

    @property
    def user_genre_headers(self):
        return ["user"] + [ascii(g) for g in self.genres]

    @property
    def user_summary_headers(self):
        return ["user", "game", "genre"]

    def _get_users(self, by="totalFollowers", direction=DESCENDING):
        if direction in [DESCENDING, ASCENDING]:
            return self.users_collection.find(QUERY_COMPLETE).sort(by, direction).limit(self._limit)
        elif direction == "split":
            return list(self.users_collection \
                        .find(QUERY_COMPLETE_BY(by)) \
                        .sort(by, mongo.ASCENDING).limit(self._limit // 2)) \
                   + list(self.users_collection.find(QUERY_COMPLETE_BY(by)) \
                          .sort(by, mongo.DESCENDING) \
                          .limit(self._limit // 2))
        elif direction == "none":
            return self.users_collection.find(QUERY_COMPLETE).limit(self._limit)
        else:
            raise RuntimeError("invalid selection=" + by)

    def user_summary(self, weighted=False):
        for doc in self.users_collection.find(
                {"$and": [QUERY_COMPLETE, {"gamesBroadcasted": {"$exists": True}}]}).sort("totalFollowers",
                                                                                          self._direction).limit(
            self._limit):
            try:
                game_names = [ele.get("game") for ele in doc["gamesBroadcasted"] if "game" in ele]
            except Exception as e:
                print(e)
            game = ""
            if len(game_names) > 0:
                game = most_frequent(game_names)
            print("game={}".format(game))
            nested = [self.games_collection.find_one({"$and": [{"game": game}, {"genres": {"$exists": True}}]}) for game
                      in game_names]
            nested = [doc["genres"] for doc in nested if doc is not None]
            genres = [genre for sublist in nested if len(sublist) > 0 for genre in sublist]
            genre = ""
            if len(genres) > 0:
                genre = most_frequent(genres)
            yield {
                "user": doc["name"],
                "game": ascii(game),
                "genre": genre
            }

    def user_rows(self):
        for doc in self._get_users():
            yield {k: v for (k, v) in doc.items() if k in self.user_headers}

    def game_rows(self):
        for doc in self.games_collection.find():
            yield {k: ascii(v) for (k, v) in doc.items() if k in self.game_headers}

    def user_followers(self):
        for doc in self._get_users():
            d = {name: 1 if name in doc["followers"] else 0 for name in self._users}
            d["user"] = doc["name"]
            yield d

    def user_following(self):
        for doc in self._get_users():
            d = {name: 1 if name in doc["following"] else 0 for name in self._users}
            d["user"] = doc["name"]
            yield d

    def user_bidirectional(self):
        for doc in self._get_users():
            d = {name: 1 if name in doc["following"] or name in doc["followers"] else 0 for name in self._users}
            d["user"] = doc["name"]
            yield d

    def user_playing(self):
        for doc in self._get_users():
            playing = [o.get("game", "") for o in doc["gamesBroadcasted"]]
            d = {ascii(g): 1 if g in playing else 0 for g in self.games}
            d["user"] = doc["name"]
            yield d

    def genres_for_game(self, game: str):
        doc = self.games_collection.find_one({"$and": [{"game": game}, {"genres": {"$exists": True}}]})
        if doc is None:
            return []
        return doc["genres"]

    def user_genre(self):
        for doc in self._get_users():
            playing = [o.get("game", "") for o in doc["gamesBroadcasted"]]
            nested = [self.genres_for_game(g) for g in playing]
            genres = [g for sublist in nested for g in sublist]
            d = {ascii(g): 1 if g in genres else 0 for g in self.genres}
            d["user"] = doc["name"]
            yield d

    @property
    def user_playing_headers(self):
        return ["user"] + [ascii(w) for w in self.games]


def most_frequent(l):
    histogram = {}
    for ele in l:
        if ele in histogram:
            histogram[ele] += 1
        else:
            histogram[ele] = 1
    return max(histogram.items(), key=lambda tup: tup[1])[0]


QUERY_COMPLETE = {"_complete": True}
QUERY_COMPLETE_BY = lambda by, x=0: {"$and": [{"_complete": True}, {by: {"$gte": x}}]}
