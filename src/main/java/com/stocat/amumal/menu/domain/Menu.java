package com.stocat.amumal.menu.domain;

import com.stocat.amumal.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {
  public enum SourceType {
    REAL,
    SYNTHETIC
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "menu_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id", nullable = false)
  private Brand brand;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column private Integer price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SourceType sourceType;

  @Column(length = 1000)
  private String sourceUrl;

  public static Menu of(
      Brand brand,
      String name,
      String description,
      Integer price,
      SourceType sourceType,
      String sourceUrl) {
    if (brand == null
        || name == null
        || name.isBlank()
        || description == null
        || sourceType == null
        || price != null && price < 0) throw new IllegalArgumentException("올바른 메뉴 정보가 필요합니다.");
    if (sourceType == SourceType.REAL && (sourceUrl == null || sourceUrl.isBlank()))
      throw new IllegalArgumentException("실제 메뉴는 출처 URL이 필요합니다.");
    Menu m = new Menu();
    m.brand = brand;
    m.name = name;
    m.description = description;
    m.price = price;
    m.sourceType = sourceType;
    m.sourceUrl = sourceUrl;
    return m;
  }
}
