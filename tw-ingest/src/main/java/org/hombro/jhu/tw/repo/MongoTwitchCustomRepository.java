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
            .setOnInsert("name", user)
            .setOnInsert("_hash", new TwitchUser().setName(user).getHash())
            .setOnInsert("_complete", false),
        TwitchUser.class);
  }

  @Override
  public void addUser(TwitchUser twitchUser) {
    log.info("adding user={}", twitchUser);
    mongoTemplate.upsert(forName(twitchUser.getName()), new Update()
            .setOnInsert("_hash", twitchUser.getHash())
            .set("bio", twitchUser.getBio())
            .set("type", twitchUser.getType())
            .set("createdAt", twitchUser.getCreatedAt())
            .set("followers", twitchUser.getFollowers())
            .set("following", twitchUser.getFollowing())
            .set("totalFollowers", twitchUser.getTotalFollowers())
            .set("totalFollowing", twitchUser.getTotalFollowing())
            .set("totalGamesBroadcasted", twitchUser.getTotalGamesBroadcasted())
            .set("gamesBroadcasted", twitchUser.getGamesBroadcasted())
            .set("_complete", twitchUser.checkCompleteness().isComplete()),
        TwitchUser.class);
  }

  @Override
  public Optional<TwitchUser> findUser(String name) {
    return Optional.ofNullable(mongoTemplate.findOne(new Query(
        new Criteria().andOperator(
            Criteria.where("name").is(name),
            Criteria.where("createdAt").ne(null)
        )
    ), TwitchUser.class));
  }

  @Override
  public void addFollower(String name, String follower) {
    addLink(name, follower, "followers");
  }

  @Override
  public void addFollowing(String name, String following) {
    addLink(name, following, "following");
  }

  private void addLink(String source, String link, String type) {
    log.info("addLink source={} link={} type={}", source, link, type);
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
    // TODO need to check for existence
    Criteria c = new Criteria().andOperator(
        Criteria.where("name").is(name),
        new Criteria().orOperator(
            Criteria.where("gamesBroadcasted").is(null),
            Criteria.where("gamesBroadcasted").elemMatch(
                Criteria.where("publishedAt").ne(game.getPublishedAt())
            )
        )
    );
    mongoTemplate.updateFirst(
        forName(name),
        new Update().addToSet("gamesBroadcasted", game),
        TwitchUser.class);
    assertGame(game.getGame());
  }

  @Override
  public void assertGame(String game) {
    /*
     TODO it seems when users post custom broadcasts there doesn't need to be a game string
     Instead of something nicer imho like "" we get null
      */
    game = game == null ? "USER_CUSTOM" : game;
    log.info("assertGame game={}", game);
    mongoTemplate.upsert(
        new Query(Criteria.where("game").is(game)),
        new Update()
            .set("game", game)
            .setOnInsert("_hash", new TwitchGame().setGame(game).getHash()),
        TwitchGame.class
    );
  }

  @Override
  public void addGame(TwitchGame twitchGame) {
    log.info("addGame game={}", twitchGame);
    mongoTemplate.upsert(
        new Query(Criteria.where("game").is(twitchGame.getGame())),
        new Update()
            .set("game", twitchGame.getGame())
            .setOnInsert("_hash", twitchGame.getHash())
            .set("popularity", twitchGame.getPopularity())
            .set("viewers", twitchGame.getViewers()),
        TwitchGame.class
    );
  }

  @Override
  public void complete(String name) {
    log.info("complete name={}", name);
    mongoTemplate
        .updateFirst(forName(name), new Update().set("_complete", true), TwitchUser.class);
  }

  @Override
  public void setBroadcastedGames(String name, List<GameBroadcast> games) {
    log.info("adding videos count={} name={}", games.size(), name);
    Update update = new Update()
        .set("gamesBroadcasted", games);
    mongoTemplate.updateFirst(forName(name), update, TwitchUser.class);
  }
}
