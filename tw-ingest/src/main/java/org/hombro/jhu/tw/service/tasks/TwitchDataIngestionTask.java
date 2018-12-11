package org.hombro.jhu.tw.service.tasks;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.core.Daemon;
import org.hombro.jhu.tw.core.thread.TaskContext;
import org.hombro.jhu.tw.core.thread.TaskContextHolder;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.commands.Command;
import org.hombro.jhu.tw.service.messaging.BrokerService;
import org.hombro.jhu.tw.service.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.MDC;

@Slf4j
public class TwitchDataIngestionTask implements Daemon {

  private final BrokerService<Command> brokerService;
  private final CommandExecutor commandExecutor;
  private final int id, totalTasks;
  private boolean flag = false;
  public TwitchDataIngestionTask(
      BrokerService<Command> brokerService,
      CommandExecutor commandExecutor,
      int id,
      int totalTasks) {
    this.id = id;
    this.totalTasks = totalTasks;
    this.brokerService = brokerService;
    this.commandExecutor = commandExecutor;
  }

  @Override
  public void init(){
    MDC.put("taskId", Integer.toString(id));
    TaskContextHolder.setContext(new TaskContext(id, totalTasks));
  }

  @Override
  public Logger getLogger() {
    return log;
  }

  @Override
  public void task() {
    Optional<Message<Command>> maybe = brokerService.dequeue();
    if (maybe.isPresent()) {
      Message<Command> msg = maybe.get();
      log.info("Handing message=" + msg);
      msg.getData().dispatch(commandExecutor).forEach(brokerService::enqueue);
    } else {

      log.info("MISS");
      flag = true;
    }
  }

  @Override
  public boolean isFinished(){
    return flag;
  }
}