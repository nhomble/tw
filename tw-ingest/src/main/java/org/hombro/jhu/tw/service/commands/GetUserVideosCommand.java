package org.hombro.jhu.tw.service.commands;

import java.util.List;
import lombok.Getter;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

@Getter
public class GetUserVideosCommand extends UserCommand {

  private GetUserVideosCommand(String user) {
    super(user);
  }

  public static GetUserVideosCommand forUser(String user){
    return new GetUserVideosCommand(user);
  }

  @Override
  public List<Message<Command>> dispatch(CommandExecutor executor) {
    return executor.handle(this);
  }

}
