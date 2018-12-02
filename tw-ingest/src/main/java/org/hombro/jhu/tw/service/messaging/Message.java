package org.hombro.jhu.tw.service.messaging;

import com.google.common.base.MoreObjects;
import lombok.Getter;

@Getter
public class Message<T> implements Comparable<Message> {

  private final String id;
  private final T data;

  private Message(String id, T data) {
    this.id = id;
    this.data = data;
  }

  public static <T> Message<T> keyedMessage(String id, T message) {
    return new Message<>(id, message);
  }

  public static <T> Message<T> newMessage(T message) {
    return new Message<>(Integer.toString(message.hashCode()), message);
  }

  @Override
  public int compareTo(Message o) {
    return id.compareTo(o.id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Message.class)
        .add("id", id)
        .add("data", data.toString())
        .toString();
  }
}
