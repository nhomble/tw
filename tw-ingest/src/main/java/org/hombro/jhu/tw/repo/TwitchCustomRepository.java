package org.hombro.jhu.tw.repo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.hombro.jhu.tw.repo.domain.GameBroadcast;
import org.hombro.jhu.tw.repo.domain.TwitchGame;
import org.hombro.jhu.tw.repo.domain.TwitchUser;

public interface TwitchCustomRepository {

  Set<String> allUsers();

  void assertUser(String user);

  void addUser(TwitchUser twitchUser);

  Optional<TwitchUser> findUser(String name);

  void addFollower(String name, String follower);

  void setFollowers(String name, List<String> followers);

  void addFollowing(String name, String following);

  void setFollowing(String name, List<String> following);

  void setBroadcastedGames(String name, List<GameBroadcast> games);

  void addBroadcastedGame(String name, GameBroadcast game);

  void assertGame(String name);

  void addGame(TwitchGame twitchGame);

  default void addGames(Iterable<String> names) {
    names.forEach(this::assertGame);
  }

  void complete(String name);
}
