package org.hombro.jhu.tw.api;

import java.util.Iterator;
import java.util.Optional;
import org.hombro.jhu.tw.api.data.kraken.TwitchFollowDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchGameDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchUserDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchVideoDTO;

public interface TwitchAPI {

  Iterator<TwitchFollowDTO> getFollowersForName(String name);

  Iterator<TwitchFollowDTO> getFollowingForName(String name);

  Iterator<TwitchVideoDTO> getVideosForName(String name);

  Optional<TwitchUserDTO> getUserByName(String name);

  Iterator<TwitchGameDTO> getTopGames();
}
