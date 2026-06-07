package com.stocat.amumal.common.config;

import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile("development")
@RequiredArgsConstructor
public class SeedConfig {

    private final UserRepository userRepository;

    @Bean
    ApplicationRunner seedRunner() {
        return arguments -> seed(); // 부트 기동 후 1회 실행
    }

    @Transactional
    void seed() {
        if (userRepository.count() >= 10) {
            return;
        }

        // tester1 ~ tester10 계정 더미 데이터
        IntStream.rangeClosed(1, 10).forEach(i -> {
            User user = User.of(
                    "tester" + i + "@stocat.com",
                    "Password1!" + i,
                    "tester" + i,
                    null
            );
            userRepository.save(user);
        });
    }
}
