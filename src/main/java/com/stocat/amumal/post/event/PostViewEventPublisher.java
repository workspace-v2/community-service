package com.stocat.amumal.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostViewEventPublisher {

    private final PostViewEventListener postViewEventListener;

    public void publishEvent(Long postId) {
        postViewEventListener.handle(new PostViewedEvent(postId));
    }
}