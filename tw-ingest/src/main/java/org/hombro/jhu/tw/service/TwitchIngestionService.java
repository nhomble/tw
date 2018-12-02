package org.hombro.jhu.tw.service;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.service.tasks.TwitchDataIngestionTask;
import org.hombro.jhu.tw.service.tasks.TwitchUserSanityTask;
import org.springframework.stereotype.Service;

@Service
@Slf4j
final public class TwitchIngestionService {

  private final List<Runnable> runnables;

  public TwitchIngestionService(List<TwitchDataIngestionTask> tasks,
      TwitchUserSanityTask sanityTask) {
    runnables = new ArrayList<>();
    runnables.add(sanityTask);
    runnables.addAll(tasks);
  }


  public void run() {
    Thread[] threads = new Thread[runnables.size()];
    for (int i = 0; i < runnables.size(); i++) {
      threads[i] = new Thread(runnables.get(i));
    }
    for (Thread t : threads) {
      log.info("Starting thread={}", t);
      t.start();
    }
    for (Thread t : threads){
      try {
        t.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    }
  }
}
