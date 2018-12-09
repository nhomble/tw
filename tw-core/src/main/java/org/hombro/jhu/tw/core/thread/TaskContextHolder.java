package org.hombro.jhu.tw.core.thread;

import org.springframework.core.NamedThreadLocal;

public class TaskContextHolder {

  private static final ThreadLocal<TaskContext> taskContextNamedThreadLocal =
      new NamedThreadLocal<>("twitch task context");

  public static void setContext(TaskContext context){
    assert getContext() == null;
    taskContextNamedThreadLocal.set(context);
  }

  public static TaskContext getContext(){
    return taskContextNamedThreadLocal.get();
  }
}
