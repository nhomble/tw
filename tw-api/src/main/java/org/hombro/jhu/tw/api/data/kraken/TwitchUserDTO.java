package org.hombro.jhu.tw.api.data.kraken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Date;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TwitchUserDTO {

  @JsonProperty("_id")
  private String id;

  private String bio;
  private String logo;
  private String name;
  private String type;

  @JsonProperty("created_at")
  private Date createdAt;

  @JsonProperty("display_name")
  private String displayName;

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(TwitchUserDTO.class)
        .add("type", type)
        .add("displayName", displayName)
        .add("name", name)
        .add("id", id)
        .toString();
  }
}
