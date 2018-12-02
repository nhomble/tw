package org.hombro.jhu.tw.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hombro.jhu.tw.repo.domain.TwitchUser;
import org.hombro.jhu.tw.service.commands.Command;
import org.hombro.jhu.tw.service.commands.GetUserCommand;
import org.hombro.jhu.tw.service.commands.GetUserFollowersCommand;
import org.hombro.jhu.tw.service.commands.GetUserFollowsCommand;
import org.hombro.jhu.tw.service.commands.GetUserVideosCommand;
import org.hombro.jhu.tw.service.messaging.Message;
import org.springframework.stereotype.Component;

@Component
final public class CommandMapper {

  private final Random rand = new Random();

  public Optional<Message<Command>> next(TwitchUser twitchUser) {
    String user = twitchUser.getName();
    List<Message<Command>> cms = Stream.of(
        twitchUser.getGamesBroadcasted() == null ?
            GetUserVideosCommand.forUser(user).asMessage() : null,
        twitchUser.getFollowers() == null ?
            GetUserFollowersCommand.forUser(user).asMessage() : null,
        twitchUser.getFollowing() == null ? GetUserFollowsCommand.forUser(user).asMessage() : null,
        twitchUser.getCreatedAt() == null ? GetUserCommand.forUser(user).asMessage() : null,
        twitchUser.getTotalFollowers() == null
            || twitchUser.getTotalFollowing() == null
            || twitchUser.getTotalGamesBroadcasted() == null
            ? GetUserCommand.forUser(user).asMessage() : null
    )
        .map(Optional::ofNullable)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
    return Optional.ofNullable(cms.isEmpty() ? null : cms.get(rand.nextInt(cms.size())));
  }
}
