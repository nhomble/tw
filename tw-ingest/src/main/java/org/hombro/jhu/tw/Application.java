package org.hombro.jhu.tw;

import org.hombro.jhu.tw.service.TwitchIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Download and some curation of twitch data into our local store
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

  @Autowired
  private TwitchIngestionService twitchIngestionService;

  public static void main(String... args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run(String... args) {
    twitchIngestionService.run();
  }
}
