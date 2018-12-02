package org.hombro.jhu.tw.api.data.kraken;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TwitchGameDTO implements PageElement {

  private GameData game;
  private int total;
  private int viewers;
  private int channels;

  @Override
  public void setTotal(int total) {
    this.total = total;
  }

  @Data
  public static class GameData {
    @JsonProperty("_id")
    private int id;
    private String name;
    private int popularity;
  }
}
