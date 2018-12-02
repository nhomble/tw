package org.hombro.jhu.tw.api.data.kraken;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class TwitchFollowPaginated extends TwitchPaginationDTO<TwitchFollowDTO> {

  private List<TwitchFollowDTO> follows = Collections.emptyList();

  @Override
  public List<TwitchFollowDTO> paginatedElements() {
    return follows;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(TwitchFollowPaginated.class)
        .add("pagination", super.toString())
        .toString();
  }
}
