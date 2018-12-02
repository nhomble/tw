package org.hombro.jhu.tw.repo.domain;

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

  @NotNull
  @Indexed(unique = true)
  private String name;

  @NotNull
  @Setter(value = AccessLevel.PRIVATE)
  @Field(value = "_hash")
  private Integer hash;

  private Date createdAt;

  private Integer totalFollowers;
  private Integer totalFollowing;
  private Integer totalGamesBroadcasted;

  private List<String> followers;
  private List<String> following;
  private List<GameBroadcast> gamesBroadcasted;

  public TwitchUser setName(String name) {
    setHash(Math.abs(name.hashCode()));
    this.name = name;
    return this;
  }
}
