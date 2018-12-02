package org.hombro.jhu.tw.api.data.kraken;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import java.util.Date;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class TwitchChannelDTO {

  @JsonProperty("_id")
  private String id;

  private boolean mature;
  private String status;
  private String broadcasterLanguage;
  private String broadcasterSoftware;
  private String displayName;
  private String game;
  private String language;
  private String name;
  private Date createdAt;
  private Date updatedAt;
  private boolean partner;
  private String logo;
  private String videoBanner;
  private String profileBanner;
  private String profileBannerBackgroundColor;
  private String url;
  private int followers;

  @JsonProperty("_links")
  private ChannelLinks links;

  private String delay;
  private String banner;
  private String background;
  private boolean notifications;

  @Data
  public static class ChannelLinks {

    private String self;
    private String follows;
    private String commercial;
    private String streamKey;
    private String chat;
    private String features;
    private String subscriptions;
    private String editors;
    private String teams;
    private String videos;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(TwitchChannelDTO.class)
        .add("name", name)
        .add("displayName", displayName)
        .toString();
  }
}
