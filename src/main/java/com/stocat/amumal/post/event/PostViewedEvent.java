package com.stocat.amumal.post.event;

// Spring 4.2 미만은 ApplicationEvent 상속을 받아야한다.
public record PostViewedEvent(Long postId) {}
