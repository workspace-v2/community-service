package com.stocat.amumal.common.config;

import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile("development")
@RequiredArgsConstructor
public class SeedConfig {

    private final UserRepository userRepository;
    @Value("${app.seed.test-user-count:20}")
    private int testUserCount;
    @Value("${app.seed.test-user-password:Password1!}")
    private String testUserPassword;

    @Bean
    ApplicationRunner seedRunner() {
        return arguments -> seed(); // 부트 기동 후 1회 실행
    }

    @Transactional
    void seed() {
        if (testUserCount <= 0) {
            return;
        }

        List<User> existingTestUsers = userRepository.findByEmailStartingWith("tester");
        int nextIndex = existingTestUsers.size() + 1;
        int usersToCreate = testUserCount - existingTestUsers.size();

        if (usersToCreate <= 0) {
            return;
        }

        // tester1 ~ testerN 계정 더미 데이터
        IntStream.range(nextIndex, nextIndex + usersToCreate).forEach(i -> {
            User user = User.of(
                    "tester" + i + "@stocat.com",
                    testUserPassword,
                    "tester" + i,
                    null
            );
            userRepository.save(user);
        });
    }
}
