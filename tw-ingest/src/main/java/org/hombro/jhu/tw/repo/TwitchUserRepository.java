package org.hombro.jhu.tw.repo;

import org.hombro.jhu.tw.repo.domain.TwitchUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TwitchUserRepository extends MongoRepository<TwitchUser, String> {


}
