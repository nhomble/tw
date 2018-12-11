package org.hombro.jhu.tw.service.messaging;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Stream;
import org.hombro.jhu.tw.config.TwitchProperties;
import org.hombro.jhu.tw.service.commands.Command;
import org.hombro.jhu.tw.service.commands.GetUserCommand;
import org.hombro.jhu.tw.service.commands.GetUserFollowsCommand;
import org.hombro.jhu.tw.service.commands.GetUserVideosCommand;
import org.hombro.jhu.tw.service.commands.UserCompleteCommand;
import org.hombro.jhu.tw.service.thread.TaskContext;
import org.hombro.jhu.tw.service.thread.TaskContextHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ConfiguredBrokerService implements BrokerService<Command> {

  private final Map<Integer, Queue<Command>> queue;

  public ConfiguredBrokerService(@Qualifier("seed") Set<String> names, TwitchProperties properties) {
    queue = new ConcurrentHashMap<>();
    for (int i = 0; i < properties.getIngestion().size(); i++) {
      queue.put(i, new ConcurrentLinkedQueue<>());
    }
    int i = 0;
    for (String s : names) {
      int id = i % properties.getIngestion().size();
      i++;
      Stream.of(
          GetUserCommand::forUser,
          (Function<String, Command>) GetUserFollowsCommand::forUser,
          GetUserVideosCommand::forUser,
          UserCompleteCommand::forUser
      ).map(sup -> sup.apply(s)).forEach(c -> queue.get(id).offer(c));
    }
  }

  @Override
  public void enqueue(Command command) {

  }

  @Override
  public void enqueue(Message<Command> data) {

  }

  @Override
  public Optional<Message<Command>> dequeue() {
    TaskContext context = TaskContextHolder.getContext();
    return Optional.ofNullable(queue.get(context.getTaskId()).poll()).map(Command::asMessage);
  }
}
