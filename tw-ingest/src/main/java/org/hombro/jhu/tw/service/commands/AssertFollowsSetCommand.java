package org.hombro.jhu.tw.service.commands;

import java.util.List;
import lombok.Getter;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

@Getter
public class AssertFollowsSetCommand implements Command {

  private final String user;

  public AssertFollowsSetCommand(String user) {
    this.user = user;
  }

  @Override
  public List<Message<Command>> dispatch(CommandExecutor executor) {
    return null;
  }

  @Override
  public Message<Command> asMessage() {
    return Message.newMessage(this);
  }
}
