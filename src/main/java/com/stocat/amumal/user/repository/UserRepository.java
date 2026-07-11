package com.stocat.amumal.user.repository;

import com.stocat.amumal.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  Optional<User> findByEmail(String email);

  List<User> findByEmailStartingWith(String emailPrefix);
}
