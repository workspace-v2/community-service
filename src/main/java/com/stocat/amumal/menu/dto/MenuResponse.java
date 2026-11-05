package com.stocat.amumal.menu.dto;

import com.stocat.amumal.menu.domain.Menu;

public record MenuResponse(
    Long id,
    Long brandId,
    String brandName,
    String name,
    String description,
    Integer price,
    Menu.SourceType sourceType,
    String sourceUrl) {
  public static MenuResponse from(Menu menu) {
    return new MenuResponse(
        menu.getId(),
        menu.getBrand().getId(),
        menu.getBrand().getName(),
        menu.getName(),
        menu.getDescription(),
        menu.getPrice(),
        menu.getSourceType(),
        menu.getSourceUrl());
  }
}
