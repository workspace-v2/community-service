package com.stocat.amumal.user.usecase;

import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final UserService userService;
    private final PostRepository postRepository;

    public void execute(Long userId) {
        userService.validateUserExists(userId);

        // TODO: 댓글 삭제
        postRepository.deleteAllByUser_Id(userId);
        userService.deleteUser(userId);
    }
}
