package com.stocat.amumal.user.usecase;

import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.user.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class DeleteUserUseCase {

    private final UserService userService;
    private final PostRepository postRepository;

    public DeleteUserUseCase(UserService userService, PostRepository postRepository) {
        this.userService = userService;
        this.postRepository = postRepository;
    }

    public void execute(Long userId) {
        userService.validateUserExists(userId);

        // TODO: 댓글 삭제
        postRepository.deleteAllByUser_Id(userId);
        userService.deleteUser(userId);
    }
}
