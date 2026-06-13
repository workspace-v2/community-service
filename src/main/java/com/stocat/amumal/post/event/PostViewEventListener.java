package com.stocat.amumal.post.event;

import com.stocat.amumal.post.service.PostViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostViewEventListener {

    private final PostViewService postViewService;

    @EventListener
    public void handle(PostViewedEvent event) {
        postViewService.incrementViewCountCache(event.postId());
    }
}