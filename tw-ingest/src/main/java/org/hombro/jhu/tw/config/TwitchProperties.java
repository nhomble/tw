package org.hombro.jhu.tw.config;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app.twitch")
@Getter
@Setter
@Validated
public class TwitchProperties {

  @NotEmpty
  private Set<String> ingestion = new HashSet<>();
}
