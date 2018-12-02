package org.hombro.jhu.tw.service;


import static org.hombro.jhu.tw.repo.domain.TwitchUser.MAX_PAGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.api.TwitchAPI;
import org.hombro.jhu.tw.api.data.kraken.TwitchFollowDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchVideoDTO;
import org.hombro.jhu.tw.repo.TwitchCustomRepository;
import org.hombro.jhu.tw.repo.domain.GameBroadcast;
import org.hombro.jhu.tw.repo.domain.TwitchGame;
import org.hombro.jhu.tw.repo.domain.TwitchUser;
import org.hombro.jhu.tw.service.commands.Command;
import org.hombro.jhu.tw.service.commands.GetAllGamesCommand;
import org.hombro.jhu.tw.service.commands.GetGameCommand;
import org.hombro.jhu.tw.service.commands.GetUserCommand;
import org.hombro.jhu.tw.service.commands.GetUserFollowersCommand;
import org.hombro.jhu.tw.service.commands.GetUserFollowsCommand;
import org.hombro.jhu.tw.service.commands.GetUserVideosCommand;
import org.hombro.jhu.tw.service.commands.UserCompleteCommand;
import org.hombro.jhu.tw.service.messaging.Message;

// TODO not reactive, we just accum locally into list and hope for no OOM
@Slf4j
final public class CommandExecutor {

  private final TwitchCustomRepository twitchUserRepository;
  private final TwitchAPI twitchAPI;

  public CommandExecutor(TwitchAPI twitchAPI, TwitchCustomRepository twitchUserRepository) {
    this.twitchAPI = twitchAPI;
    this.twitchUserRepository = twitchUserRepository;
  }

  private <E> List<Message<Command>> handle(Iterator<E> it, Consumer<E> sideEffect,
      Function<E, Command> producer) {
    List<Message<Command>> ret = new ArrayList<>();
    for (int max = 0; max < MAX_PAGE && it.hasNext(); max++) {
      E e = it.next();
      sideEffect.accept(e);
      ret.add(producer.apply(e).asMessage());
    }
    return ret;
  }

  public List<Message<Command>> handle(GetUserCommand getUserCommand) {
    if (twitchUserRepository.findUser(getUserCommand.getUser()).isPresent()) {
      return Collections.emptyList();
    }
    return twitchAPI.getUserByName(getUserCommand.getUser()).map(dto -> {

      Iterator<TwitchVideoDTO> vI = twitchAPI.getVideosForName(getUserCommand.getUser());
      Iterator<TwitchFollowDTO> fingI = twitchAPI.getFollowingForName(getUserCommand.getUser());
      Iterator<TwitchFollowDTO> fwerI = twitchAPI.getFollowersForName(getUserCommand.getUser());

      int vT = vI.hasNext() ? vI.next().getTotal() : 0;
      int gT = fingI.hasNext() ? fingI.next().getTotal() : 0;
      int rT = fwerI.hasNext() ? fwerI.next().getTotal() : 0;

      log.info("add user={}", getUserCommand.getUser());
      twitchUserRepository.addUser(new TwitchUser()
          .setCreatedAt(dto.getCreatedAt())
          .setTotalFollowers(rT)
          .setTotalFollowing(gT)
          .setTotalGamesBroadcasted(vT)
          .setName(dto.getName()));
      return Arrays.asList(
          GetUserVideosCommand.forUser(getUserCommand.getUser()).asMessage(),
          GetUserFollowsCommand.forUser(getUserCommand.getUser()).asMessage(),
          GetUserFollowersCommand.forUser(getUserCommand.getUser()).asMessage()
      );
    }).orElse(Collections.emptyList());
  }

  public List<Message<Command>> handle(GetUserFollowersCommand getUserFollowersCommand) {
    return handle(
        twitchAPI.getFollowersForName(getUserFollowersCommand.getUser()),
        follower -> twitchUserRepository
            .addFollower(getUserFollowersCommand.getUser(), follower.getName()),
        follower -> GetUserCommand.forUser(follower.getName())
    );
  }

  public List<Message<Command>> handle(GetUserFollowsCommand getUserFollowsCommand) {
    return handle(
        twitchAPI.getFollowingForName(getUserFollowsCommand.getUser()),
        following -> twitchUserRepository
            .addFollowing(getUserFollowsCommand.getUser(), following.getName()),
        following -> GetUserCommand.forUser(following.getName())
    );
  }

  public List<Message<Command>> handle(GetUserVideosCommand getUserVideosCommand) {
    twitchAPI.getVideosForName(getUserVideosCommand.getUser()).forEachRemaining(dto -> {
      twitchUserRepository.addBroadcastedGame(getUserVideosCommand.getUser(),
          new GameBroadcast()
              .setCreatedAt(dto.getCreatedAt())
              .setPublishedAt(dto.getPublishedAt())
              .setGame(dto.getGame())
              .setBroadcastType(dto.getBroadcastType())
              .setStatus(dto.getStatus())
              .setTitle(dto.getTitle())
              .setLength(dto.getLength())
              .setViews(dto.getViews())
      );
    });
    return Collections.emptyList();
  }

  public List<Message<Command>> handle(UserCompleteCommand userCompleteCommand) {
    twitchUserRepository.complete(userCompleteCommand.getUser());
    return Collections.emptyList();
  }

  public List<Message<Command>> handle(GetGameCommand getGameCommand) {
    throw new UnsupportedOperationException("no api for this");
  }

  public List<Message<Command>> handle(GetAllGamesCommand getAllGamesCommand) {
    twitchAPI.getTopGames().forEachRemaining(dto -> twitchUserRepository.addGame(new TwitchGame()
        .setViewers(dto.getViewers())
        .setPopularity(dto.getGame().getPopularity())
        .setChannels(dto.getChannels())
        .setGame(dto.getGame().getName())));
    return Collections.emptyList();
  }
}
