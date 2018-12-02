package org.hombro.jhu.tw.service.commands;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import org.hombro.jhu.tw.service.messaging.Message;

@Getter
public abstract class GameCommand implements Command {

  private final String game;

  protected GameCommand(String game) {
    this.game = game;
  }

  @Override
  public Message<Command> asMessage() {
    return Message.keyedMessage(getClass().getCanonicalName() + "=" + game, this);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("game", game)
        .toString();
  }

}
