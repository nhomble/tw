package org.hombro.jhu.tw.repo;

import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Accessors(chain = true)
public class GameBroadcast {

  @Indexed
  private String game;
  private Date createdAt;
  private Date publishedAt;
  private int views;
  private int length;
  private String broadcastType;
  private String status;
  private String title;

}
