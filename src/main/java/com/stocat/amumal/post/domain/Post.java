package com.stocat.amumal.post.domain;

import com.stocat.amumal.common.entity.BaseEntity;
import com.stocat.amumal.menu.domain.Menu;
import com.stocat.amumal.user.domain.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 26)
  private String title;

  // 기존 종목 게시글은 메뉴를 임의 배정하지 않고 보존한다.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "menu_id")
  private Menu menu;

  private Integer rating;

  @ElementCollection
  @CollectionTable(name = "post_tag", joinColumns = @JoinColumn(name = "post_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "tag", nullable = false, length = 30)
  private Set<ReviewTag> tags = new HashSet<>();

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(length = 500)
  private String imageUrl;

  @Column(nullable = false)
  private int commentCount = 0;

  @Column(nullable = false)
  private int likeCount = 0;

  @Column(nullable = false)
  private long viewCount = 0;

  public static Post of(
      User user,
      Menu menu,
      int rating,
      Set<ReviewTag> tags,
      String title,
      String content,
      String imageUrl) {
    Post post = new Post();
    post.user = user;
    if (menu == null) throw new IllegalArgumentException("메뉴가 필요합니다.");
    validateReview(rating, tags);
    post.menu = menu;
    post.rating = rating;
    post.tags.addAll(tags);
    post.title = title;
    post.content = content;
    post.imageUrl = imageUrl;
    return post;
  }

  private static void validateReview(int rating, Set<ReviewTag> tags) {
    if (rating < 1
        || rating > 5
        || tags == null
        || tags.stream().anyMatch(java.util.Objects::isNull))
      throw new IllegalArgumentException("평점은 1~5, 태그는 유효한 값이어야 합니다.");
  }

  public void update(
      String title, String content, String imageUrl, int rating, Set<ReviewTag> tags) {
    validateReview(rating, tags);
    this.rating = rating;
    this.tags.clear();
    this.tags.addAll(tags);
    this.title = title;
    this.content = content;
    this.imageUrl = imageUrl;
  }

  public void increaseCommentCount() {
    this.commentCount += 1;
  }

  public void decreaseCommentCount() {
    this.commentCount = Math.max(0, this.commentCount - 1);
  }

  public void increaseLikeCount() {
    this.likeCount += 1;
  }

  public void decreaseLikeCount() {
    this.likeCount = Math.max(0, this.likeCount - 1);
  }
}
