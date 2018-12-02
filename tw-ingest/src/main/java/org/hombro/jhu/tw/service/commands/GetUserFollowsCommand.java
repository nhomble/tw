package org.hombro.jhu.tw.service.commands;

import java.util.List;
import lombok.Getter;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

@Getter
public class GetUserFollowsCommand implements Command {

  private final String user;

  private GetUserFollowsCommand(String user) {
    this.user = user;
  }

  public static GetUserFollowsCommand forUser(String user){
    return new GetUserFollowsCommand(user);
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
