package org.hombro.jhu.tw.api.data.kraken;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TwitchStreamDTO {

  private StreamData stream;

  @Data
  static class StreamData {

    private String game;
  }
}
