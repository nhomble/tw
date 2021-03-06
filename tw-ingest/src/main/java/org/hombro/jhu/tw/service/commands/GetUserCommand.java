package org.hombro.jhu.tw.service.commands;

import java.util.List;
import lombok.Getter;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

@Getter
public class GetUserCommand extends UserCommand {

  public GetUserCommand(String user) {
    super(user);
  }

  public static GetUserCommand forUser(String user) {
    return new GetUserCommand(user);
  }

  @Override
  public List<Message<Command>> dispatch(CommandExecutor executor) {
    return executor.handle(this);
  }


}
