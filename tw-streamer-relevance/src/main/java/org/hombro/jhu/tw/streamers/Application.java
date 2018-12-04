package org.hombro.jhu.tw.streamers;

import org.hombro.jhu.tw.streamers.services.FollowersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

  @Autowired
  private FollowersService followersService;

  public static void main(String... args) {
    SpringApplication.run(Application.class, args);
  }

  public void run(String... args) throws Exception {
    followersService.doIt();
  }
}
