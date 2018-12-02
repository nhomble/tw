package org.hombro.jhu.tw.api.data.kraken;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.sun.istack.internal.NotNull;
import java.util.List;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public abstract class TwitchPaginationDTO<E extends PageElement> {

  @JsonProperty("_total")
  private int total;

  @JsonProperty("_links")
  @NotNull
  private PaginationLinks links;

  @JsonIgnore
  public abstract List<E> paginatedElements();

  @Data
  public static class PaginationLinks {

    private String self;
    private String next;
  }

  @Override
  public String toString(){
    return MoreObjects.toStringHelper(TwitchPaginationDTO.class)
        .add("total", total)
        .toString();
  }
}
