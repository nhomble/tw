package org.hombro.jhu.tw.api.data.kraken;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TwitchGamePaginated extends TwitchPaginationDTO<TwitchGameDTO>  {

  private List<TwitchGameDTO> top = Collections.emptyList();

  @Override
  public List<TwitchGameDTO> paginatedElements() {
    return top;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(TwitchGamePaginated.class)
        .add("pagination", super.toString())
        .toString();
  }
}
