package com.stocat.amumal.post.event;

import com.stocat.amumal.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PostViewEventListener {

    private final PostService postService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostViewedEvent event) {
        postService.incrementViewCountCache(event.postId());
    }
}