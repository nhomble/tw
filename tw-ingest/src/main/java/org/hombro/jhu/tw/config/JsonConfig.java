package org.hombro.jhu.tw.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfig {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer customizer() {
    return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.failOnUnknownProperties(false);
  }
}
