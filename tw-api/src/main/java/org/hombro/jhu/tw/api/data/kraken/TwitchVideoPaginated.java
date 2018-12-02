package org.hombro.jhu.tw.api.data.kraken;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class TwitchVideoPaginated extends TwitchPaginationDTO<TwitchVideoDTO> {

  private List<TwitchVideoDTO> videos = Collections.emptyList();

  @Override
  public List<TwitchVideoDTO> paginatedElements() {
    return videos;
  }

  @Override
  public String toString(){
    return MoreObjects.toStringHelper(TwitchVideoPaginated.class)
        .add("pagination", super.toString())
        .toString();
  }
}
