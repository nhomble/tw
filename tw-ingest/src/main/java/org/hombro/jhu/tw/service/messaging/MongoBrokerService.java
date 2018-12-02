package org.hombro.jhu.tw.service.messaging;

import java.util.Arrays;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.repo.domain.TwitchUser;
import org.hombro.jhu.tw.service.CommandMapper;
import org.hombro.jhu.tw.service.commands.Command;
import org.hombro.jhu.tw.service.commands.GetUserCommand;
import org.hombro.jhu.tw.service.thread.TaskContext;
import org.hombro.jhu.tw.service.thread.TaskContextHolder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MongoBrokerService implements BrokerService<Command> {

  private final CommandMapper commandMapper;
  private final MongoTemplate mongoTemplate;
  private final Queue<Message<Command>> seed;

  public MongoBrokerService(CommandMapper commandMapper, MongoTemplate mongoTemplate) {
    this.commandMapper = commandMapper;
    this.mongoTemplate = mongoTemplate;
    seed = new ConcurrentLinkedQueue<>(Arrays.asList(
        GetUserCommand.forUser("shroud").asMessage(),
        GetUserCommand.forUser("serral").asMessage(),
        GetUserCommand.forUser("loltyler1").asMessage()
    ));
  }

  @Override
  public void enqueue(Command command) {
    // do nothing because we expect side effects to already occur in the repository
  }

  @Override
  public void enqueue(Message<Command> data) {
    // do nothing because we expect side effects to already occur in the repository
  }

  @Override
  public Optional<Message<Command>> dequeue() {
    if (!seed.isEmpty()) {
      return Optional.ofNullable(seed.poll());
    }
    TaskContext context = TaskContextHolder.getContext();
    Query query = new Query(
        new Criteria().andOperator(
            Criteria.where("_hash").mod(context.getTotalTasks(), context.getTaskId()),
            new Criteria().orOperator(
                Criteria.where("createdAt").is(null),
                Criteria.where("gamesBroadcasted").is(null),
                Criteria.where("following").is(null),
                Criteria.where("followers").is(null),
                Criteria.where("totalFollowers").is(null),
                Criteria.where("totalFollowing").is(null),
                Criteria.where("totalGamesBroadcasted").is(null)
            )
        ));
    return Optional.ofNullable(mongoTemplate.findOne(query, TwitchUser.class)).flatMap(
        commandMapper::next);
  }
}
