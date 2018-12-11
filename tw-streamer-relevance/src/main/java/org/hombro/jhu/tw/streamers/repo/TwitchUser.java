package org.hombro.jhu.tw.streamers.repo;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "users")
@Data
@Accessors(chain = true)
public class TwitchUser {
  public static int MAX_PAGE = 2000;

  @NotNull
  @Indexed(unique = true)
  private String name;

  @NotNull
  @Setter(value = AccessLevel.PRIVATE)
  @Field(value = "_hash")
  private Integer hash;

  private Date createdAt;

  private String bio;
  private String type;

  @Indexed
  private Integer totalFollowers;
  private Integer totalFollowing;
  private Integer totalGamesBroadcasted;

  private List<String> followers;
  private List<String> following;
  private List<GameBroadcast> gamesBroadcasted = Collections.emptyList();

  @Field(value = "_complete")
  private boolean complete;

  public TwitchUser checkCompleteness() {
    complete = false;
    return this;
  }

  public TwitchUser setName(String name) {
    setHash(Math.abs(name.hashCode()));
    this.name = name;
    return this;
  }
}
