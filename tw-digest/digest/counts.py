import argparse

import digest


def main(host: str, port: int, db_name: str):
    store = digest.TwitchDataStore.connect_to(host, port, db_name, direction="none", limit=10000)
    total = 0
    tally = {
        "fifa": 0,
        "lol": 0,
        "csgo": 0,
        "dota": 0,
        "cod": 0,
        "ds": 0,
        "bf": 0,
        "monster": 0,
        "fortnite": 0,
        "detroit": 0,
        "destiny": 0,
        "creative": 0,
        "dayz": 0,
        "diablo": 0,
        "asmr": 0
    }

    diablo_games = ["Diablo II: Lord of Destruction", "Diablo III", "Diablo III: Reaper of Souls"]
    destiny_games = [ "Destiny", "Destiny 2"]
    fifa_games = ["FIFA {}".format(i) for i in range(14, 20)] + ["FIFA Soccer 13", "fifa14"]
    dota_games = ["Dota 2", "Dota 2 1"]
    cod_games = [
        "Call of Duty: " + suffix for suffix in
        ["Black Ops 4", "Black Ops II", "Black Ops III", "Ghosts", "Infinite Warfare", "Modern Warfare Remastered",
         "World at War: Zombies", "WWII"]
    ]
    ds_games = ["Dark Souls", "Dark Souls II", "Dark Souls III"]
    bf_games = ["Battlefield 1", "Battlefield 3", "Battlefield 4"]

    for doc in store.users_collection.find():

        games = [obj.get("game", "") for obj in doc["gameBroadcasts"]]
        total += 1
        if any(g in games for g in diablo_games):
            tally["diablo"] += 1
        if "Detroit: Become Human" in games:
            tally["detroit"] += 1
        if "DayZ" in games:
            tally["dayz"] += 1
        if "Creative" in games:
            tally["creative"] += 1
        if "ASMR" in games:
            tally["asmr"] += 1
        if "Counter Strike: Global Offensive" in games:
            tally["csgo"] += 1
        if "League of Legends" in games:
            tally["lol"] += 1
        if any([g in games for g in fifa_games]):
            tally["fifa"] += 1
        if any([g in games for g in dota_games]):
            tally["dota"] += 1
        if any([g in games for g in cod_games]):
            tally["cod"] += 1
        if any([g in games for g in ds_games]):
            tally["ds"] += 1
        if any([g in games for g in bf_games]):
            tally["bf"] += 1
        if "Monster Hunter World" in games:
            tally["monster"] += 1
        if "Fortnite" in games:
            tally["fortnite"] += 1
    print(tally)
    print("total: {}".format(total))

    print("==== POPULARITY ==== ")
    all_games = {
        "asmr": ["ASMR"],
        "creative": ["Creative"],
        "dayz": ["DayZ"],
        "detroit": ["Detroit: Become Human"],
        "diablo": diablo_games,
        "destiny": destiny_games,
        "battlefield": bf_games,
        "dark souls": ds_games,
        "fortnite": ["Fortnite"],
        "monster": ["Monster Hunter World"],
        "League": ["League of Legends"],
        "CSGO": ["Counter-Strike: Global Offensive"],
        "battle": bf_games,
        "dota": dota_games,
        "fifa": fifa_games
    }
    d = {}
    for (game, queries) in all_games.items():
        d[game] = {"viewers": 0, "popularity": 0}
        for doc in store.games_collection.find({"$or": [{"game": g} for g in queries]}):
            d[game]["viewers"] += doc.get("viewers", 0)
            d[game]["popularity"] += doc.get("popularity", 0)

    for (k, v) in d.items():
        print("game={}".format(k))
        print("data={}".format(v))
    print(d)


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db)
