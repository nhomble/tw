package org.hombro.jhu.tw.config;

import static com.google.common.collect.Streams.zip;
import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.hombro.jhu.tw.api.SpringTwitchAPI;
import org.hombro.jhu.tw.api.TwitchAPI;
import org.hombro.jhu.tw.services.StreamerNetworkService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@EnableConfigurationProperties(StreamerProperties.class)
public class StreamerConfig {

  @Bean
  public List<TwitchAPI> apis(StreamerProperties properties, final RestTemplateBuilder builder) {
    return properties.getClients()
        .stream()
        .map(client -> new SpringTwitchAPI(client, builder))
        .collect(Collectors.toList());
  }

  @Bean(value = "tasks")
  public List<StreamerNetworkService> services(List<TwitchAPI> apis, MongoTemplate mongoTemplate)
      throws IOException {
    List<Set<String>> buckets = range(0, apis.size()).mapToObj(i -> new HashSet<String>())
        .collect(Collectors.toList());
    PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    List<String> streamers = Stream
        .of(IOUtils
            .toString(resourcePatternResolver.getResources("names.csv")[0].getInputStream(),
                "UTF-8")
            .split("\n"))
        .map(String::trim)
        .collect(Collectors.toList());
    for (int i = 0; i < streamers.size(); i++) {
      int id = i % apis.size();
      buckets.get(id).add(streamers.get(i));
    }
    return zip(apis.stream(), buckets.stream(), (api, bucket) -> new StreamerNetworkService(
        new HashSet<>(streamers), bucket, api,
        mongoTemplate))
        .collect(Collectors.toList());
  }
}
