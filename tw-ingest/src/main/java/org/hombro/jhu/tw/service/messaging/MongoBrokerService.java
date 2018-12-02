package org.hombro.jhu.tw.service.messaging;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.repo.domain.TwitchUser;
import org.hombro.jhu.tw.service.CommandMapper;
import org.hombro.jhu.tw.service.commands.Command;
import org.hombro.jhu.tw.service.commands.GetAllGamesCommand;
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
    seed = Stream.of(
        "shroud",
        "lotyler1",
        "tofusenshi",
        "Theonemanny",
        "404BrokenBlade",
        "MissCoookiez",
        "deadmau5",
        "stray228",
        "djmariio"
    ).map(user -> GetUserCommand.forUser(user).asMessage()
    ).collect(Collectors.toCollection(ConcurrentLinkedQueue::new));

    seed.offer(new GetAllGamesCommand().asMessage());
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
    Criteria modCriteria = Criteria.where("_hash")
        .mod(context.getTotalTasks(), context.getTaskId());
    Query userQuery = new Query(
        new Criteria().andOperator(
            modCriteria,
            new Criteria().orOperator(
                Criteria.where("_complete").is(false)
            )
        ));

    return Optional
        .ofNullable(mongoTemplate.findOne(userQuery, TwitchUser.class))
        .flatMap(commandMapper::next);
  }
}
