// CodeRabbit evaluates this factory server-side; no npm dependency is required.
// Commit this file to PR head branches (dev/features) as well as main.
export default (ctx) => {
  // main <- dev is disabled: baseBranch is the destination, not the source.
  const autoReview = ctx.pr?.baseBranch === "dev";

  return {
    language: "ko-KR",
    tone_instructions:
      "재현 가능한 결함과 성능·정합성 문제를 우선 지적하세요. 근거 없이 문제를 단정하지 말고, CI 포맷 검사와 중복되는 스타일 지적은 줄이세요.",
    reviews: {
      profile: "quiet",
      high_level_summary: true,
      high_level_summary_in_walkthrough: true,
      high_level_summary_instructions:
        "변경한 동작과 목적을 한국어 3~4개 항목으로 요약하세요. 실측되지 않은 성능 개선 수치를 만들지 마세요.",
      collapse_walkthrough: true,
      in_progress_fortune: false,
      poem: false,
      path_filters: ["!build/**", "!.gradle/**", "!src/main/generated/**"],
      path_instructions: [
        {
          path: "src/main/java/**",
          instructions:
            "버거 메뉴·리뷰 검색 백엔드입니다. 이번 PR의 변경 범위에서 실제 결함과 회귀를 우선 검토하세요. JPA 연관관계 접근에 따른 N+1, 컬렉션 fetch join과 페이지네이션의 조합, 중복·누락 및 정렬·필터·커서 동작 변경, 트랜잭션과 원본·검색 데이터 정합성을 확인하세요. 검색 최적화 시 기존 API 응답과 검색 의미가 유지되는지 검토하세요. 캐시·인덱스·새 기술은 확인된 병목과 트레이드오프 없이 무조건 권하지 마세요. 기존 문제는 이번 변경이 유발하거나 악화한 문제와 구분하세요.",
        },
        {
          path: "src/test/java/**",
          instructions:
            "구현을 그대로 따라가는 검증보다 반환 결과·경계 조건·실패 상황·회귀를 검증하는지 확인하세요. SQL 횟수 검증은 flush/clear 등으로 1차 캐시 영향을 제거하고, 서로 다른 연관 엔티티 및 빈 결과·복수 태그·페이지 경계를 포함하는지 확인하세요. 시간 기반 테스트에 임의의 성능 임계값을 강요하지 마세요.",
        },
        {
          path: "src/main/resources/db/migration/**",
          instructions:
            "MySQL 대상 마이그레이션입니다. 기존 데이터 보존, 기존 마이그레이션 불변성, 제약 조건·인덱스의 필요성과 배포 순서의 호환성을 검토하세요. 인덱스 추가만으로 LIKE 부분 문자열 검색과 정렬 비용이 해결된다고 단정하지 마세요.",
        },
        {
          path: "**/*.md",
          instructions:
            "성능 수치는 실측 결과인지 계획·목표인지 구분하는지 확인하세요. 비교 수치에는 데이터 규모·부하·서버 조건·반복 횟수 근거가 있어야 하며, 단일 실행 결과나 SQL 횟수 감소를 최대 처리량 또는 검색 정확도 향상으로 확대 해석하지 않도록 검토하세요.",
        },
      ],
      auto_review: {
        enabled: autoReview,
        auto_incremental_review: autoReview,
        base_branches: ["^dev$"],
        drafts: false,
        // Prevent label/description opt-ins from enabling main automatic reviews.
        labels: [],
        description_keyword: "",
      },
    },
  };
};
