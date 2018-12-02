package org.hombro.jhu.tw.service.commands;

import com.google.common.base.MoreObjects;
import java.util.List;
import lombok.Getter;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

@Getter
public class GetUserCommand implements Command {

  private final String user;

  public GetUserCommand(String user) {
    this.user = user;
  }

  public static GetUserCommand forUser(String user) {
    return new GetUserCommand(user);
  }

  @Override
  public List<Message<Command>> dispatch(CommandExecutor executor) {
    return executor.handle(this);
  }

  @Override
  public Message<Command> asMessage() {
    return Message.keyedMessage(GetUserCommand.class.getCanonicalName() + "=" + user, this);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(GetUserCommand.class)
        .add("user", user)
        .toString();
  }
}
