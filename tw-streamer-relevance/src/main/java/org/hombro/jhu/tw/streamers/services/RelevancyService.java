package org.hombro.jhu.tw.streamers.services;


import java.util.Iterator;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.api.TwitchAPI;
import org.hombro.jhu.tw.api.data.kraken.TwitchFollowDTO;
import org.hombro.jhu.tw.streamers.repo.TwitchLink;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
public class RelevancyService implements Runnable {

  private static int MAX_FOLLOWERS = 500;
  private static int MAX_FOLLOWING = 500;

  private final Set<String> streamers;
  private final TwitchAPI api;
  private final MongoTemplate mongoTemplate;

  public RelevancyService(Set<String> streamers, TwitchAPI api,
      MongoTemplate mongoTemplate) {
    this.streamers = streamers;
    this.api = api;
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public void run() {
    streamers.forEach(streamer -> {
      Iterator<TwitchFollowDTO> followers = api.getFollowersForName(streamer);
      for (int i = 0; i < MAX_FOLLOWERS && followers.hasNext(); i++) {
        TwitchFollowDTO dto = followers.next();
        String follower = dto.getName();
        Iterator<TwitchFollowDTO> following = api.getFollowingForName(follower);
        for (int j = 0; j < MAX_FOLLOWING && following.hasNext(); j++) {
          TwitchFollowDTO anotherDto = following.next();
          String anotherStreamer = anotherDto.getName();
          TwitchLink link = new TwitchLink()
              .setFollower(follower)
              .setLinkedStreamer(anotherStreamer)
              .setSourceStreamer(streamer);
          if (!streamer.equals(anotherStreamer)) {
            log.info("asserting link={}", link);
            mongoTemplate.save(link);
          }
        }
      }
    });
    System.out.print(streamers);
  }
}
