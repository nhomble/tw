package org.hombro.jhu.tw.service.tasks;

import static java.util.stream.IntStream.range;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.repo.TwitchCustomRepository;
import org.hombro.jhu.tw.repo.TwitchUserRepository;
import org.hombro.jhu.tw.repo.domain.TwitchUser;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * While the broker is good about finding incomplete data and scheduling it, we still want to assert
 * followers/follwings were mapped
 */
@Slf4j
public class TwitchUserSanityTask implements Daemon {

  private final TwitchCustomRepository twitchRepo;
  private final PagingAndSortingRepository<TwitchUser, String> repository;

  public TwitchUserSanityTask(TwitchCustomRepository twitchRepo,
      TwitchUserRepository repository) {
    this.twitchRepo = twitchRepo;
    this.repository = repository;
  }

  @Override
  public Logger getLogger() {
    return log;
  }

  @Override
  public void task() {
    long count = repository.count();
    int size = 100;
    int pages = (int) (count / size);

    log.info("Running sanity check count={} size={} pages={}", count, size, pages);

    range(0, pages).forEach(pageNum -> {
      PageRequest request = PageRequest.of(pageNum, size);
      repository.findAll(request).forEach(twitchUser -> {
        Optional.ofNullable(twitchUser.getFollowing())
            .ifPresent(l -> l.forEach(twitchRepo::assertUser));
        Optional.ofNullable(twitchUser.getFollowers())
            .ifPresent(l -> l.forEach(twitchRepo::assertUser));
      });
    });
  }
}
