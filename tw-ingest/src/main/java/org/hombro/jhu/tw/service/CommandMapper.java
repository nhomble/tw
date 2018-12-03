package org.hombro.jhu.tw.service;

import static org.hombro.jhu.tw.repo.domain.TwitchUser.MAX_PAGE;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hombro.jhu.tw.repo.domain.TwitchGame;
import org.hombro.jhu.tw.repo.domain.TwitchUser;
import org.hombro.jhu.tw.service.commands.Command;
import org.hombro.jhu.tw.service.commands.GetGameCommand;
import org.hombro.jhu.tw.service.commands.GetUserCommand;
import org.hombro.jhu.tw.service.commands.GetUserFollowersCommand;
import org.hombro.jhu.tw.service.commands.GetUserFollowsCommand;
import org.hombro.jhu.tw.service.commands.GetUserVideosCommand;
import org.hombro.jhu.tw.service.commands.UserCommand;
import org.hombro.jhu.tw.service.commands.UserCompleteCommand;
import org.hombro.jhu.tw.service.messaging.Message;
import org.springframework.stereotype.Component;

@Component
final public class CommandMapper {

  public Optional<Message<Command>> next(TwitchUser twitchUser) {
    String user = twitchUser.getName();
    List<Message<Command>> cms = Stream.of(

        twitchUser.getCreatedAt() == null ? GetUserCommand.forUser(user) : null,
        twitchUser.getTotalFollowers() == null
            || twitchUser.getTotalFollowing() == null
            || twitchUser.getTotalGamesBroadcasted() == null
            ? GetUserCommand.forUser(user) : null,

        twitchUser.getGamesBroadcasted().size() < MAX_PAGE
            && twitchUser.getGamesBroadcasted().size() < Optional
            .ofNullable(twitchUser.getTotalGamesBroadcasted()).orElse(Integer.MAX_VALUE) * .70 ?
            GetUserVideosCommand.forUser(user) : null,

        twitchUser.getFollowers().size() < Optional.ofNullable(twitchUser.getTotalFollowers())
            .orElse(Integer.MAX_VALUE) * .70 &&
            twitchUser.getFollowers().size() < MAX_PAGE - 5 ?
            GetUserFollowersCommand.forUser(user) : null,

        twitchUser.getFollowing().size() <
            Optional.ofNullable(twitchUser.getTotalFollowing()).orElse(Integer.MAX_VALUE) * .70 &&
            twitchUser.getFollowing().size() < MAX_PAGE
            ? GetUserFollowsCommand.forUser(user) : null,

        twitchUser.isComplete() != twitchUser.checkCompleteness().isComplete() ? UserCompleteCommand
            .forUser(user) : null
    )
        .map(Optional::ofNullable)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(UserCommand::asMessage)
        .collect(Collectors.toList());
    return cms.stream().findFirst();
  }

  public Optional<Message<Command>> next(TwitchGame twitchGame) {
    return Optional.of(GetGameCommand.forGame(twitchGame.getGame()).asMessage());
  }
}
