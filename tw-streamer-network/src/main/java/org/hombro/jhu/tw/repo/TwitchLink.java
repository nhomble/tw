package org.hombro.jhu.tw.repo;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document(collection = "links")
@CompoundIndex(def = "{'sourceStreamer':1, 'linkedStreamer':1, 'follower':1}", unique = true)
public class TwitchLink {

  @Indexed
  private String sourceStreamer;
  @Indexed
  private String linkedStreamer;
  @Indexed
  private String follower;

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(TwitchLink.class)
        .add("sourceStreamer", sourceStreamer)
        .add("linkedStreamer", linkedStreamer)
        .add("follower", follower)
        .toString();
  }
}
