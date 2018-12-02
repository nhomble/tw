package org.hombro.jhu.tw.repo.domain;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "games")
@Data
@Accessors(chain = true)
public class TwitchGame {

  private String game;
  private int popularity;
  private int viewers;
  private int channels;

  @NotNull
  @Setter(value = AccessLevel.PRIVATE)
  @Field(value = "_hash")
  private Integer hash;

  public TwitchGame setGame(String game){
    setHash(Math.abs(game.hashCode()));
    this.game = game;
    return this;
  }
}
