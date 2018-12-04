package org.hombro.jhu.tw.streamers.services;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.api.TwitchAPI;
import org.hombro.jhu.tw.api.data.kraken.TwitchFollowDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchUserDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchVideoDTO;
import org.hombro.jhu.tw.streamers.repo.GameBroadcast;
import org.hombro.jhu.tw.streamers.repo.TwitchLink;
import org.hombro.jhu.tw.streamers.repo.TwitchUser;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Slf4j
public class RelevancyService implements Runnable {

  private static int MAX_STREAMERS = 100;
  private static int MAX_FOLLOWERS = 100;
  private static int MAX_FOLLOWING = 100;

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
    Iterator<String> streams = streamers.iterator();
    for (int k = 0; k < MAX_STREAMERS && streams.hasNext(); k++) {
      String streamer = streams.next();
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
            log.info("asserting link={} streamerCount={} followerCount={} otherStreamerCount={}",
                link, k, i, j);
            mongoTemplate.upsert(
                new Query(
                    new Criteria().andOperator(
                        Criteria.where("sourceStreamer").is(streamer),
                        Criteria.where("linkedStreamer").is(anotherStreamer),
                        Criteria.where("follower").is(follower)
                    )
                ),
                new Update()
                    .setOnInsert("sourceStreamer", streamer)
                    .setOnInsert("linkedStreamer", anotherStreamer)
                    .setOnInsert("follower", follower),
                TwitchLink.class
            );
          }

          Stream.of(streamer, anotherStreamer, follower).forEach(user -> {
            if (!mongoTemplate.exists(new Query(
                new Criteria().andOperator(
                    Criteria.where("name").is(user),
                    Criteria.where("_complete").is(true)
                )
            ), TwitchUser.class)) {
              TwitchUserDTO userDTO = api.getUserByName(user)
                  .orElseThrow(() -> new RuntimeException(user));
              Iterator<TwitchVideoDTO> videoDTOIterator = api.getVideosForName(user);
              List<GameBroadcast> broadcasts = new ArrayList<>();
              Iterator<TwitchFollowDTO> _followers = api.getFollowersForName(user);
              Iterator<TwitchFollowDTO> _following = api.getFollowingForName(user);

              TwitchUser newUser = new TwitchUser()
                  .setName(user)
                  .setComplete(true)
                  .setCreatedAt(userDTO.getCreatedAt())
                  .setTotalFollowers(_followers.hasNext() ? _followers.next().getTotal() : 0)
                  .setTotalFollowing(_following.hasNext() ? _following.next().getTotal() : 0);
              videoDTOIterator.forEachRemaining(video -> {
                newUser.setTotalGamesBroadcasted(video.getTotal());
                broadcasts.add(new GameBroadcast()
                    .setGame(video.getGame()));
              });

              newUser.setGamesBroadcasted(broadcasts);
              log.info("adding new user={}", newUser);
              mongoTemplate.upsert(new Query(
                      Criteria.where("name").is(user)
                  ),
                  new Update()
                      .set("name", newUser.getName())
                      .set("bio", newUser.getBio())
                      .set("type", newUser.getType())
                      .set("_complete", newUser.isComplete())
                      .set("createdAt", newUser.getCreatedAt())
                      .set("totalFollowers", newUser.getTotalFollowers())
                      .set("totalFollowing", newUser.getTotalFollowing())
                      .set("totalGamesBroadcasted", newUser.getTotalGamesBroadcasted())
                      .set("gameBroadcasts", newUser.getGamesBroadcasted()),
                  TwitchUser.class
              );
            }
          });
        }
      }
    }
  }
}
