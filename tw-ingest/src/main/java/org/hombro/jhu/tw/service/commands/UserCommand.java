package org.hombro.jhu.tw.service.commands;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import org.hombro.jhu.tw.service.messaging.Message;

@Getter
public abstract class UserCommand implements Command {

  private final String user;

  protected UserCommand(String user) {
    this.user = user;
  }

  @Override
  public Message<Command> asMessage() {
    return Message.keyedMessage(getClass().getCanonicalName() + "=" + user, this);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("user", user)
        .toString();
  }
}
