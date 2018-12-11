package org.hombro.jhu.tw.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.hombro.jhu.tw.api.SpringTwitchAPI;
import org.hombro.jhu.tw.repo.TwitchCustomRepository;
import org.hombro.jhu.tw.repo.TwitchUserRepository;
import org.hombro.jhu.tw.service.CommandExecutor;
import org.hombro.jhu.tw.service.commands.Command;
import org.hombro.jhu.tw.service.messaging.BrokerService;
import org.hombro.jhu.tw.service.tasks.TwitchDataIngestionTask;
import org.hombro.jhu.tw.service.tasks.TwitchUserSanityTask;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@EnableConfigurationProperties(TwitchProperties.class)
public class TwitchConfig {

  @Bean
  public List<TwitchDataIngestionTask> tasks(TwitchProperties twitchProperties,
      RestTemplateBuilder restTemplateBuilder,
      TwitchCustomRepository twitchCustomRepository,
      MongoTemplate mongoTemplate,
      BrokerService<Command> brokerService) {
    List<CommandExecutor> executors = twitchProperties.getIngestion().stream()
        .map(key -> new SpringTwitchAPI(key, restTemplateBuilder))
        .map(client -> new CommandExecutor(mongoTemplate, client, twitchCustomRepository))
        .collect(Collectors.toList());
    List<TwitchDataIngestionTask> t = new ArrayList<>();
    for (int i = 0; i < executors.size(); i++) {
      t.add(new TwitchDataIngestionTask(brokerService, executors.get(i), i, executors.size()));
    }
    return t;
  }

  @Bean
  public TwitchUserSanityTask twitchUserSanityTask(TwitchCustomRepository customRepository,
      TwitchUserRepository twitchUserRepository) {
    return new TwitchUserSanityTask(customRepository, twitchUserRepository);
  }

  @Bean(name = "seed")
  public Set<String> seed(TwitchProperties twitchProperties) throws IOException {
    PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    return Stream
        .of(IOUtils.toString(resourcePatternResolver.getResources("names.csv")[0].getInputStream(), "UTF-8")
            .split("\n"))
        .map(String::trim)
        .collect(Collectors.toSet());
  }
}
