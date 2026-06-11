package com.stocat.amumal.post.event;

import com.stocat.amumal.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostViewEventListener {

    private final PostService postService;

    @EventListener
    public void handle(PostViewedEvent event) {
        postService.incrementViewCountCache(event.postId());
    }
}