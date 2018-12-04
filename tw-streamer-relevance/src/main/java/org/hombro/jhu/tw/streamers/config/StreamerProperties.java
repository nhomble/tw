package org.hombro.jhu.tw.streamers.config;

import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.twitch")
@Data
public class StreamerProperties {

  private Set<String> clients;
}
