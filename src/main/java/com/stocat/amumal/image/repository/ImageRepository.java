package com.stocat.amumal.image.repository;

import com.stocat.amumal.image.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
