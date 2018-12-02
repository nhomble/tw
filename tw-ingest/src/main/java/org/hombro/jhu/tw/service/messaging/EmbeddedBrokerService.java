package org.hombro.jhu.tw.service.messaging;

import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * A dirty, quick, and just crap TWITCH_BROKER that relies on a local multithreaded java queue.
 * There is no backup.
 */
@Slf4j
public class EmbeddedBrokerService<T> implements BrokerService<T> {

  private final Set<String> seen;
  private final Queue<Message<T>> queue;

  public EmbeddedBrokerService(Set<String> seen, Queue<Message<T>> queue) {
    this.seen = seen;
    this.queue = queue;
  }

  public void enqueue(T data) {
    queue.offer(Message.newMessage(data));
  }

  @Override
  public void enqueue(Message<T> data) {
    if (!seen.contains(data.getId())) {
      log.info("We have not seen messageId={}", data.getId());
      queue.offer(data);
    }
    log.info("We have seen messageId={}", data.getId());
  }

  public Optional<Message<T>> dequeue() {
    return Optional.ofNullable(queue.poll()).filter(m -> !seen.contains(m.getId()));
  }

  public void addSeen(String s) {
    seen.add(s);
  }

  public void addSeen(Set<String> more) {
    seen.addAll(more);
  }
}
