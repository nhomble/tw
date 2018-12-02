package org.hombro.jhu.tw.service.messaging;

import java.util.Optional;

public interface BrokerService<DATA> {

  void enqueue(DATA data);

  void enqueue(Message<DATA> data);

  Optional<Message<DATA>> dequeue();
}
