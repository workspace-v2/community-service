package com.stocat.amumal.image.repository;

import com.stocat.amumal.image.domain.Image;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
  Optional<Image> findByFileUrl(String fileUrl);
}
