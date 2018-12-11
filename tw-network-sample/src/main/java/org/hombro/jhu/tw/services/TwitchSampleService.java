package org.hombro.jhu.tw.services;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TwitchSampleService {

  private final List<TwitchSamplerService> services;

  public TwitchSampleService(
      @Qualifier("tasks") List<TwitchSamplerService> services) {
    this.services = services;
  }

  public void doIt(){
    ExecutorService service = Executors.newFixedThreadPool(services.size());
    services.forEach(service::execute);
    service.shutdown();
  }
}
