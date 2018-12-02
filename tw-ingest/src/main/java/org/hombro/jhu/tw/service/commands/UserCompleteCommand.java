package org.hombro.jhu.tw.service.commands;

import java.util.List;
import lombok.Getter;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

@Getter
public class UserCompleteCommand extends UserCommand {

  private UserCompleteCommand(String user) {
    super(user);
  }

  public static UserCompleteCommand forUser(String user){
    return new UserCompleteCommand(user);
  }

  @Override
  public List<Message<Command>> dispatch(CommandExecutor executor) {
    return executor.handle(this);
  }
}
