package org.hombro.jhu.tw.services;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.api.TwitchAPI;
import org.hombro.jhu.tw.api.data.kraken.TwitchFollowDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchUserDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchVideoDTO;
import org.hombro.jhu.tw.repo.GameBroadcast;
import org.hombro.jhu.tw.repo.TwitchLink;
import org.hombro.jhu.tw.repo.TwitchUser;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Slf4j
public class TwitchSamplerService implements Runnable {

  private static final int TOTAL_FOLLOWING = 50;
  private static final int TOTAL_FOLLOWERS = 50;
  private static final int TOTAL_LEVELS = 3;
  private final Set<String> streamers;
  private final TwitchAPI api;
  private final MongoTemplate mongoTemplate;

  public TwitchSamplerService(Set<String> streamers,
      TwitchAPI api,
      MongoTemplate mongoTemplate) {
    this.streamers = streamers;
    this.api = api;
    this.mongoTemplate = mongoTemplate;
  }

  private TwitchUser getCompleteUser(String name) {
    TwitchUserDTO userDTO = api.getUserByName(name)
        .orElseThrow(() -> new RuntimeException(name));
    Iterator<TwitchVideoDTO> videoDTOIterator = api.getVideosForName(name);
    List<GameBroadcast> broadcasts = new ArrayList<>();
    Iterator<TwitchFollowDTO> _followers = api.getFollowersForName(name);
    Iterator<TwitchFollowDTO> _following = api.getFollowingForName(name);

    TwitchUser newUser = new TwitchUser()
        .setName(name)
        .setComplete(true)
        .setBio(userDTO.getBio())
        .setType(userDTO.getType())
        .setCreatedAt(userDTO.getCreatedAt())
        .setTotalFollowers(_followers.hasNext() ? _followers.next().getTotal() : 0)
        .setTotalFollowing(_following.hasNext() ? _following.next().getTotal() : 0);
    videoDTOIterator.forEachRemaining(video -> {
      newUser.setTotalGamesBroadcasted(video.getTotal());
      broadcasts.add(new GameBroadcast()
          .setGame(video.getGame()));
    });

    newUser.setGamesBroadcasted(broadcasts);
    return newUser;
  }

  private boolean userExists(String name) {
    return mongoTemplate.exists(new Query(
            new Criteria().andOperator(
                Criteria.where("name").is(name),
                Criteria.where("_complete").is(true)
            )),
        TwitchUser.class
    );
  }

  private void insertUser(TwitchUser twitchUser) {
    log.debug("adding new user={}", twitchUser);
    mongoTemplate.upsert(new Query(
            Criteria.where("name").is(twitchUser.getName())
        ),
        new Update()
            .set("name", twitchUser.getName())
            .set("bio", twitchUser.getBio())
            .set("type", twitchUser.getType())
            .set("_complete", twitchUser.isComplete())
            .set("createdAt", twitchUser.getCreatedAt())
            .set("totalFollowers", twitchUser.getTotalFollowers())
            .set("totalFollowing", twitchUser.getTotalFollowing())
            .set("totalGamesBroadcasted", twitchUser.getTotalGamesBroadcasted())
            .set("gameBroadcasts", twitchUser.getGamesBroadcasted()),
        TwitchUser.class
    );
  }

  private void link(String from, String to) {
    log.debug("sourceStreamer={} linkedStreamer={}", from, to);
    mongoTemplate.upsert(
        new Query(new Criteria().andOperator(
            Criteria.where("linkedStreamer").is(to),
            Criteria.where("follower").is(from),
            Criteria.where("sourceStreamer").is(from)
        )),
        new Update()
            .setOnInsert("linkedStreamer", to)
            .setOnInsert("follower", from)
            .setOnInsert("sourceStreamer", from),
        TwitchLink.class);
  }

  private void assertUser(String name) {
    if (!userExists(name)) {
      TwitchUser streamerUser = getCompleteUser(name);
      insertUser(streamerUser);
    }
  }

  @Override
  public void run() {
    Set<String> names = streamers;

    for (int run = 0; run < TOTAL_LEVELS; run++) {
      Set<String> accum = new HashSet<>();
      for (String top : names) {
        try {
          assertUser(top);
          Iterator<TwitchFollowDTO> peopleStreamerFollows = api.getFollowingForName(top);
          for (int i = 0; i < TOTAL_FOLLOWING && peopleStreamerFollows.hasNext(); i++) {
            TwitchFollowDTO following = peopleStreamerFollows.next();
            assertUser(following.getName());
            accum.add(following.getName());
            log.info("Following level={} from={} to={}, i={}", run, top, following.getName(), i);
          }
          Iterator<TwitchFollowDTO> followersOfStreamer = api.getFollowersForName(top);
          for (int j = 0; j < TOTAL_FOLLOWERS && followersOfStreamer.hasNext(); j++) {
            TwitchFollowDTO follower = followersOfStreamer.next();
            assertUser(follower.getName());
            accum.add(follower.getName());
            log.info("Follower level={} from={} to={}, j={}", run, top, follower.getName(), j);
          }
        } catch (Exception e) {
          log.error("Failed somewhere", e);
        }
      }

      names = accum;
    }
  }
}
