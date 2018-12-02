package org.hombro.jhu.tw.service.thread;

import lombok.Getter;

@Getter
public class TaskContext {

  private final int taskId;
  private final int totalTasks;

  public TaskContext(int taskId, int totalTasks) {
    this.taskId = taskId;
    this.totalTasks = totalTasks;
  }
}
