package org.hombro.jhu.tw.core;

import org.slf4j.Logger;

public interface Daemon extends Runnable {

  @Override
  default void run() {
    init();
    while (!isFinished()) {
      try {
        task();
      } catch (Throwable t) {
        getLogger().error("Exception in thread msg=" + t.getMessage() + " RECOVERING", t);
      }
      if (throttle() != 0) {
        try {
          Thread.sleep(throttle());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          getLogger().error("Thread was interrupted", e);
        }
      }
    }
  }

  default long throttle() {
    return 0;
  }

  default void init() {
  }

  Logger getLogger();

  void task();

  default boolean isFinished(){
    return false;
  }
}
