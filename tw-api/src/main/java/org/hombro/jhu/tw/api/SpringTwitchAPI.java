package org.hombro.jhu.tw.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.hombro.jhu.tw.api.data.kraken.PageElement;
import org.hombro.jhu.tw.api.data.kraken.TwitchFollowDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchFollowPaginated;
import org.hombro.jhu.tw.api.data.kraken.TwitchGameDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchGamePaginated;
import org.hombro.jhu.tw.api.data.kraken.TwitchPaginationDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchUserDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchVideoDTO;
import org.hombro.jhu.tw.api.data.kraken.TwitchVideoPaginated;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class SpringTwitchAPI implements TwitchAPI {

  private static final String HOST = "api.twitch.tv";
  private static final String TWITCH_V5 = "application/vnd.twitchtv.v5+json";
  private static final String HEADER_CLIENT = "Client-ID";
  private final String clientId;
  private final URIBuilder builder;
  private final RestTemplate restTemplate;

  public SpringTwitchAPI(String clientId, RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
    this.clientId = clientId;
    builder = URIBuilder.withClient(clientId);
  }

  private static class URIBuilder {

    private final UriComponentsBuilder uriComponentsBuilder;

    private URIBuilder(UriComponentsBuilder uriComponentsBuilder) {
      this.uriComponentsBuilder = uriComponentsBuilder;
    }

    static URIBuilder withClient(String clientId) {
      return new URIBuilder(UriComponentsBuilder.newInstance()
          .scheme("https")
          .queryParam("client_id", clientId)
          .host(HOST));
    }

    static URIBuilder fromURI(URI uri) {
      return new URIBuilder(UriComponentsBuilder.fromUri(uri));
    }

    public UriComponentsBuilder with() {
      return uriComponentsBuilder.cloneBuilder();
    }

    public URI built() {
      return with().build(Collections.emptyMap());
    }
  }

  @Nullable
  private <T> T get(URI uri, Class<T> response) {
    log.debug("GET responseClass={} uri={}", response.getCanonicalName(), uri);
    HttpEntity entity = new HttpEntity<>(
        ImmutableMap.of(HttpHeaders.ACCEPT, TWITCH_V5));
    try {
      return restTemplate.exchange(URIBuilder.fromURI(uri).with()
          .queryParam("client_id", clientId)
          .build(Collections.emptyMap()), HttpMethod.GET, entity, response).getBody();
    } catch (HttpClientErrorException | HttpServerErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return null;
      }
      Objects.requireNonNull(e.getResponseHeaders())
          .forEach(
              (key, value) -> log.error("header={} headerValue={} exception={}", key, value, e));
      log.error("uri={} headers={} e={}", uri, e.getResponseHeaders().toString(), e);
      throw new RuntimeException("Failed to get URI=" + uri, e);
    }
  }

  static class PaginatedResponse<E extends PageElement, T extends TwitchPaginationDTO<E>> implements
      Iterator<E> {

    private final Class<T> response;
    private final SpringTwitchAPI api;
    private URI uri;

    private int total;
    private int seen, last;
    private Iterator<E> local = Collections.emptyIterator();

    private PaginatedResponse(SpringTwitchAPI api, Class<T> response) {
      this.api = api;
      this.response = response;

      total = Integer.MAX_VALUE;
      last = -1;
      seen = 0;
    }

    private void refresh() {
      T pagination = Objects.requireNonNull(api.get(uri, response));
      computeNext(pagination);

      last = seen;
      total = pagination.getTotal();
      local = pagination.paginatedElements().iterator();
      log.debug("paging total={} last={} current={} uri={}", total, last, seen, uri);
    }

    private void computeNext(T pagination) {
      try {
        uri = URIBuilder.fromURI(new URI(pagination.getLinks().getNext())).with()
            .queryParam("limit", Math.min(100, total - seen))
            .build(Collections.emptyMap());
      } catch (URISyntaxException e) {
        log.error("URI={} pagination={} exception={}", uri, pagination, e);
        throw new RuntimeException(e);
      }
    }

    static <E extends PageElement, T extends TwitchPaginationDTO<E>> PaginatedResponse<E, T> forResponse(
        SpringTwitchAPI api,
        Class<T> response) {
      return new PaginatedResponse<>(api, response);
    }

    static <E extends PageElement, T extends TwitchPaginationDTO<E>> PaginatedResponse<E, T> forResponse(
        SpringTwitchAPI api,
        Class<T> response, URI uri) {
      return forResponse(api, response).withURI(uri);
    }

    PaginatedResponse<E, T> withURI(URI uri) {
      this.uri = uri;
      return this;
    }

    @Override
    public boolean hasNext() {
      if (total == 0) {
        return false;
      }
      if (local.hasNext()) {
        return true;
      }
      if (last != seen) {
        refresh();
        return local.hasNext();
      }
      throw new IllegalStateException("Iterator cannot come here");
    }

    @Override
    public E next() {
      seen += 1;
      E ele = local.next();
      ele.setTotal(total);
      return ele;
    }
  }

  @SuppressWarnings("unchecked")
  private <E extends PageElement, T extends TwitchPaginationDTO<E>> Iterator<E> getPaginated(
      URI uri,
      Class<T> response) {
    return PaginatedResponse.forResponse(this, response, uri);
  }

  @Override
  public Iterator<TwitchFollowDTO> getFollowersForName(String name) {
    URI uri = builder.with()
        .path("/kraken/channels/{user}/follows")
        .build(name);
    return getPaginated(uri, TwitchFollowPaginated.class);
  }

  @Override
  public Iterator<TwitchFollowDTO> getFollowingForName(String name) {
    URI uri = builder.with()
        .path("/kraken/users/{id}/follows/channels")
        .build(name);
    return getPaginated(uri, TwitchFollowPaginated.class);
  }

  @Override
  public Iterator<TwitchVideoDTO> getVideosForName(String name) {
    URI uri = builder.with()
        .path("/kraken/channels/{name}/videos")
        .build(name);
    return getPaginated(uri, TwitchVideoPaginated.class);
  }

  @Override
  public Optional<TwitchUserDTO> getUserByName(String name) {
    URI uri = builder.with()
        .path("/kraken/users/{userId}")
        .build(name);
    return Optional.ofNullable(get(uri, TwitchUserDTO.class));
  }

  @Override
  public Iterator<TwitchGameDTO> getTopGames() {
    URI uri = builder.with()
        .path("/kraken/games/top")
        .build(Collections.emptyMap());
    /*
    TODO so even though we can get a total, this model breaks the pattern as far as I can tell
    because we can only get data for the first 20
     */
    List<TwitchGameDTO> ret = new ArrayList<>();
    Iterator<TwitchGameDTO> it = getPaginated(uri, TwitchGamePaginated.class);
    for (int i = 0; i < 20; i++) {
      assert it.hasNext() : "Failed to find next where i=" + i;
      ret.add(it.next());
    }
    return ret.iterator();
  }
}
