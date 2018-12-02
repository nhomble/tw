package org.hombro.jhu.tw.service.commands;

import java.util.List;
import lombok.Getter;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

@Getter
public class GetUserFollowersCommand implements Command {

  private final String user;

  private GetUserFollowersCommand(String user) {
    this.user = user;
  }

  public static GetUserFollowersCommand forUser(String user){
    return new GetUserFollowersCommand(user);
  }

  @Override
  public List<Message<Command>> dispatch(CommandExecutor executor) {
    return executor.handle(this);
  }

  @Override
  public Message<Command> asMessage() {
    return Message.keyedMessage(this.getClass().getCanonicalName() + "=" + user, this);
  }
}
