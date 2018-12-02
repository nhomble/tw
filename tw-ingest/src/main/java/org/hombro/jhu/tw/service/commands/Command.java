package org.hombro.jhu.tw.service.commands;

import java.util.List;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.messaging.Message;

public interface Command {

  List<Message<Command>> dispatch(CommandExecutor executor);

  Message<Command> asMessage();
}
