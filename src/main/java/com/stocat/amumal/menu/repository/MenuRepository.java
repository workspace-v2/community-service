package com.stocat.amumal.menu.repository;

import com.stocat.amumal.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {}
