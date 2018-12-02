package org.hombro.jhu.tw.api.data.kraken;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class TwitchVideoDTO implements PageElement {

  @JsonProperty("_id")
  private String id;

  private String title;
  private String description;
  private String broadcastType;
  private String status;
  private String language;
  private int views;
  private Date createdAt;
  private Date publishedAt;
  private String url;
  private String game;
  private int length;

  private int total;

  @Override
  public void setTotal(int total) {
    this.total = total;
  }
}
