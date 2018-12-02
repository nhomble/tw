package org.hombro.jhu.tw.api.data.kraken;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import java.util.Date;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class TwitchFollowDTO implements PageElement{

  private Date createdAt;
  @JsonProperty("_links")
  private TwitchFollowingLinks links;
  private TwitchChannelDTO channel;
  private boolean notifications;

  private int total;

  @Override
  public void setTotal(int total) {
    this.total = total;
  }

  @Data
  public static class TwitchFollowingLinks {

    private String self;
  }

  public String getName() {
    if (channel != null) {
      return channel.getName();
    }
    String[] parts = links.getSelf().split("/");
    return parts[parts.length - 4];
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(TwitchFollowDTO.class)
        .add("createdAt", createdAt)
        .add("channel", channel)
        .toString();
  }
}
