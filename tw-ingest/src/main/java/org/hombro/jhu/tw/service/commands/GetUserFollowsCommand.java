package org.hombro.jhu.tw.service.commands;

import java.util.List;
import lombok.Getter;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

@Getter
public class GetUserFollowsCommand extends UserCommand {

  private GetUserFollowsCommand(String user) {
    super(user);
  }

  public static GetUserFollowsCommand forUser(String user) {
    return new GetUserFollowsCommand(user);
  }

  @Override
  public List<Message<Command>> dispatch(CommandExecutor executor) {
    return executor.handle(this);
  }

}
