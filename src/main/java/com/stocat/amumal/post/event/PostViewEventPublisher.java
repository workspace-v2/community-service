package com.stocat.amumal.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostViewEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public void publishEvent(Long postId) {
    applicationEventPublisher.publishEvent(new PostViewedEvent(postId));
  }
}
