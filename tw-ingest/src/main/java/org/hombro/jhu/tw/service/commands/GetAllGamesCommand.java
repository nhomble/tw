package org.hombro.jhu.tw.service.commands;

import java.util.List;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

public class GetAllGamesCommand implements Command {

  @Override
  public List<Message<Command>> dispatch(CommandExecutor executor) {
    return executor.handle(this);
  }

  @Override
  public Message<Command> asMessage() {
    return Message.newMessage(this);
  }
}
