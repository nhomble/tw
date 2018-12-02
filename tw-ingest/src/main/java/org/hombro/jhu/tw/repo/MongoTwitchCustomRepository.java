package org.hombro.jhu.tw.repo;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.repo.domain.GameBroadcast;
import org.hombro.jhu.tw.repo.domain.TwitchGame;
import org.hombro.jhu.tw.repo.domain.TwitchUser;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class MongoTwitchCustomRepository implements TwitchCustomRepository {

  private final MongoTemplate mongoTemplate;

  public MongoTwitchCustomRepository(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  private Query forName(String name) {
    return new Query(Criteria.where("name").is(name));
  }

  @Override
  public Set<String> allUsers() {
    Set<String> ret = new HashSet<>();
    mongoTemplate.findAll(TwitchUser.class).forEach(user -> ret.add(user.getName()));
    return ret;
  }

  @Override
  public void assertUser(String user) {
    mongoTemplate.upsert(forName(user),
        new Update()
            .set("name", user)
            .set("_hash", new TwitchUser().setName(user).getHash()),
        TwitchUser.class);
  }

  @Override
  public void addUser(TwitchUser twitchUser) {
    log.info("adding user={}", twitchUser);
    mongoTemplate.upsert(forName(twitchUser.getName()), new Update()
            .set("_hash", twitchUser.getHash())
            .set("createdAt", twitchUser.getCreatedAt())
            .set("followers", twitchUser.getFollowers())
            .set("following", twitchUser.getFollowing())
            .set("totalFollowers", twitchUser.getTotalFollowers())
            .set("totalFollowing", twitchUser.getTotalFollowing())
            .set("totalGamesBroadcasted", twitchUser.getTotalGamesBroadcasted())
            .set("gamesBroadcasted", twitchUser.getGamesBroadcasted()),
        TwitchUser.class);
  }

  @Override
  public Optional<TwitchUser> findUser(String name) {
    return Optional.ofNullable(mongoTemplate.findOne(forName(name), TwitchUser.class));
  }

  @Override
  public void addFollower(String name, String follower) {
    addLink(name, follower, "follower");
  }

  @Override
  public void addFollowing(String name, String following) {
    addLink(name, following, "following");
  }

  private void addLink(String source, String link, String type) {
    log.info("addLink source={} link={} type={}", source, link, type);
    Query q = new Query(new Criteria().andOperator(
        Criteria.where("name").is(source),
        Criteria.where(type).is(null)
    ));
    mongoTemplate.updateFirst(
        q,
        new Update()
            .push(type, link),
        TwitchUser.class
    );
    mongoTemplate.updateFirst(forName(source), new Update()
        .addToSet(type, link), TwitchUser.class);
    assertUser(link);
  }

  private void seedNewUsers(List<String> names) {
    names.forEach(this::assertUser);
  }

  @Override
  public void setFollowers(String name, List<String> followers) {
    log.info("adding followers count={} name={}", followers.size(), name);
    Update update = new Update()
        .set("followers", followers);
    mongoTemplate.updateFirst(forName(name), update, TwitchUser.class);

    seedNewUsers(followers);
  }

  @Override
  public void setFollowing(String name, List<String> following) {
    log.info("adding following count={} name={}", following.size(), name);
    Update update = new Update()
        .set("following", following);
    mongoTemplate.updateFirst(forName(name), update, TwitchUser.class);

    seedNewUsers(following);
  }

  @Override
  public void addBroadcastedGame(String name, GameBroadcast game) {
    Criteria c = new Criteria().andOperator(
        Criteria.where("name").is(name),
        new Criteria().orOperator(
            Criteria.where("gamesBroadcasted").is(null),
            Criteria.where("gamesBroadcasted").elemMatch(
                Criteria.where("game").ne(game.getGame())
            )
        )
    );
    mongoTemplate.updateFirst(
        new Query(c),
        new Update().addToSet("gamesBroadcasted", game),
        TwitchUser.class);
  }

  @Override
  public void addGame(String game) {
    log.info("addGame game={}");
    mongoTemplate.upsert(
        new Query(Criteria.where("game").is(game)),
        new Update()
            .set("game", game)
            .set("_hash", new TwitchGame().setGame(game).getHash()),
        TwitchGame.class
    );
  }

  @Override
  public void setBroadcastedGames(String name, List<GameBroadcast> games) {
    log.info("adding videos count={} name={}", games.size(), name);
    Update update = new Update()
        .set("gamesBroadcasted", games);
    mongoTemplate.updateFirst(forName(name), update, TwitchUser.class);
  }
}
