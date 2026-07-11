package com.stocat.amumal.user.repository;

import com.stocat.amumal.user.domain.UserImage;
import com.stocat.amumal.user.domain.UserImageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserImageRepository extends JpaRepository<UserImage, UserImageId> {
  void deleteAllById_UserId(Long userId);
}
