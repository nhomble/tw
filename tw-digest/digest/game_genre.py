"""
The crux of our argument here is that we can model the twitch social networks by game genre because of how the skills
and similar gameplay transfer over.

The hypothesis is someone is more likely to follow someone under the same genre than just under the same game.
You could why would people follow another game of the same genre, just pick what you like.
"""

import argparse
import re
import digest
import wikipedia
import bs4
import uuid


def get_genres(page):
    html = page.html()
    bs = bs4.BeautifulSoup(html, "html.parser")

    # TODO this is crazy ugly and the library should really parse this into a dict for people
    try:
        table = bs.find_all('table', attrs={"class": "infobox"})[0]
        text = list([ele for ele in table.find_all("a", text=True) if 'genre' in ele.get_text().lower()][
                        0].parent.parent.children)[
            1].get_text().lower()
    except Exception as e:
        text = page.title.lower()
    # some help cause it aint a list

    pattern = re.compile(r'\s\s+')
    text = re.sub(pattern, " ", text)

    pattern = re.compile(r'\[\d+\]')
    text = re.sub(pattern, '', text)

    accum = []
    for w in ["collectible", "online", "simulation", "strategy", "platform", "adventure", "puzzle"]:
        if w in text:
            accum.append(w)
            text = text.replace(w, "")
    parts = text.split(",")
    if len(parts) > 0 and len(parts[0]) > 0:
        accum += parts
    return list(set([ele.strip() for ele in accum]))


def _wpage(query: str):
    search = wikipedia.search(query, results=10, suggestion=True)
    if search[1] is None:
        try:
            return wikipedia.page(search[0][0])
        except Exception as e:
            pass
    return None


def main(host: str, port: int, db_name: str):
    store = digest.TwitchDataStore.connect_to(host, port, db_name)

    i = 0
    for g in store.games_collection.find():
        print("We are at i={}".format(i))
        i += 1

        name = g["game"]
        if len(name) > 0:
            page = _wpage(name)
            if page is not None:
                genres = get_genres(page)
                print(",".join(genres))
                print(store.games_collection.find_and_modify(query={"game": name}, update={"$set": {"genres": genres}},
                                                             upsert=False, full_response=True))


if __name__ == "__main__":
    _parser = argparse.ArgumentParser()
    _parser.add_argument("--data", default="localhost")
    _parser.add_argument("--port", default=27017)
    _parser.add_argument("--db", required=True)
    _args = _parser.parse_args()
    main(_args.data, _args.port, _args.db)
