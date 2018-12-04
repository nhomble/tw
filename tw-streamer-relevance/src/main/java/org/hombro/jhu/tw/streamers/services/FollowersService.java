package org.hombro.jhu.tw.streamers.services;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FollowersService {

  private final List<RelevancyService> services;

  public FollowersService(
      @Qualifier("tasks") List<RelevancyService> services) {
    this.services = services;
  }

  public void doIt(){
    ExecutorService service = Executors.newFixedThreadPool(services.size());
    services.forEach(service::execute);
    service.shutdown();
  }
}
