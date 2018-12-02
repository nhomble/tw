package org.hombro.jhu.tw.service.commands;

import java.util.List;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

public class GetGameCommand extends GameCommand {

  private GetGameCommand(String game) {
    super(game);
  }

  public static GetGameCommand forGame(String game){
    return new GetGameCommand(game);
  }

  @Override
  public List<Message<Command>> dispatch(CommandExecutor executor) {
    return executor.handle(this);
  }
}
