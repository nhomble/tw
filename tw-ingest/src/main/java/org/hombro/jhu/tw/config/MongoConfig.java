package org.hombro.jhu.tw.config;

import org.hombro.jhu.tw.repo.TwitchCustomRepository;
import org.hombro.jhu.tw.repo.TwitchUserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
    basePackageClasses = {
        TwitchUserRepository.class,
        TwitchCustomRepository.class
    })
public class MongoConfig {

}
