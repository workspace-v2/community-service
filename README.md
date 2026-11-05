# Burgerpedia — 버거 리뷰와 검색 커뮤니티

기존 `Post`와 `/v1/posts` CRUD를 버거 리뷰 게시글로 전환했다. 별도의 Review 엔티티·CRUD는 없다. 메뉴 정보와 리뷰 게시글을 MySQL LIKE로 검색하는 기준 구현이며, Elasticsearch 도입 전 성능·품질 비교의 출발점이다.

Java 21, Spring Boot, JPA/Querydsl, MySQL, Redis를 사용한다. Elasticsearch/Nori/BM25/Outbox는 아직 구현하지 않았다. Redis는 기존 로그인 토큰·조회수·캐시 용도다. `market-data-service`에 대한 호출과 종목 동기화는 이 서비스에서 제거했다.

## 도메인과 데이터 보존

- `brand` → `menu` → `post`: 하나의 메뉴에 여러 버거 리뷰가 연결된다.
- 새 리뷰에는 `menuId`, 정수 평점 `rating`(1~5), 제목·본문이 필요하다. 태그는 선택이며 생략하면 빈 집합이다.
- 메뉴 연결은 작성 후 바꿀 수 없다. 수정에서는 평점·태그·제목·본문·이미지를 함께 전달한다. 태그 생략 시 기존 태그가 비워진다.
- 기존 댓글·좋아요·이미지 매핑·조회수 처리를 재사용한다. 기존 댓글·좋아요·이미지 매핑 삭제 처리에는 변경을 추가하지 않는다. 새 태그는 Post의 요소 컬렉션으로 관리한다.
- V1~V5는 이미 적용된 이력으로 보존하고 V6가 카탈로그와 리뷰 컬럼을 추가한다. 기존 종목 게시글에 임의의 메뉴나 평점을 부여하지 않는다. 기존 행·symbol·stock 테이블은 DB에 보존하지만 버거 목록·검색·상세·수정에는 노출하지 않는다. 과거 글의 수동 매핑/보관 정책은 별도 작업이다.
- `menu.sourceType`은 `REAL` 또는 `SYNTHETIC`이다. `REAL`에는 출처 URL이 필수다. 가격은 원 단위 정수이며 미확인 가격은 NULL이다. 실제 브랜드·메뉴 수집과 대규모 리뷰 생성은 아직 수행하지 않았다.

## HTTP API

기본 경로는 `/v1`, 응답은 기존 `{code, message, data}` 형식이다. 카탈로그·리뷰 GET은 공개다. 리뷰 쓰기의 기존 인증 경로는 그대로 유지한다. 기존 JWT 필터가 `/posts` 쓰기도 제외하여 사용자 ID가 전달되지 않는 문제는 이번 기능 추가에서 수정하지 않는다.

| 메서드·경로 | 내용 |
|---|---|
| GET `/brands?offset=0&limit=20` | 브랜드 목록 |
| GET `/menus?keyword=치즈&brandId=1&maxPrice=7000&sort=relevance` | 메뉴명·설명·브랜드명 LIKE 검색, 브랜드·가격 필터 |
| GET `/menus/{menuId}` | 메뉴 상세·데이터 출처 |
| POST `/posts` | 기존 게시글 작성 경로로 버거 리뷰 생성 |
| PATCH `/posts/{postId}` | 기존 게시글 수정 경로로 리뷰 수정 |
| DELETE `/posts/{postId}` | 기존 게시글 삭제 경로로 리뷰 삭제 |
| GET `/posts/{postId}` | 메뉴·브랜드·평점·태그를 포함한 리뷰 상세 |
| GET `/posts?offset=0&limit=10` | 리뷰 목록 |
| GET `/posts/cursor?menuId=1&limit=10&cursor=123` | 메뉴별 커서 목록, menuId 생략 시 전체 |
| GET `/posts/search?keyword=치즈&menuId=1&offset=0&limit=10&sort=relevance` | 리뷰 제목·본문·메뉴명·브랜드명·태그 검색 |
| GET `/search?keyword=치즈&menuOffset=0&reviewOffset=0&limit=10&sort=relevance` | 메뉴와 리뷰를 함께 반환, 각각 독립 페이지 |

`limit`은 1~100, offset은 0~10000, 검색어는 공백 제외 1~100자다. 검색어 앞뒤 공백을 제거한다. 메뉴 목록의 keyword는 생략할 수 있다. 응답의 `hasNext`는 limit+1개를 조회해 결정하며 총 개수 쿼리는 수행하지 않는다.

`sort`는 `recent` 또는 `relevance`다. relevance는 제목(메뉴는 이름) 포함 우선, 다음 본문(메뉴는 설명) 포함, 마지막 ID 내림차순이다. BM25 점수나 메뉴·리뷰를 섞은 전역 순위가 아니다. 통합 응답은 `data.menus`, `data.reviews` 두 묶음이며 각 offset과 hasNext를 반환한다.

작성 예시:

```json
{
  "menuId": 1,
  "rating": 4,
  "tags": ["CHEESY", "THICK_PATTY"],
  "title": "두꺼운 패티가 좋았던 버거",
  "content": "치즈 맛이 진하고 패티에 육즙이 있었다.",
  "attachFileUrl": null
}
```

수정은 위 필드 중 menuId를 제외한다. 허용 태그: `SPICY`(매콤), `THICK_PATTY`(두꺼운 패티), `GOOD_VALUE`(가성비), `CHEESY`(치즈), `JUICY`(육즙), `CRISPY`(바삭), `HANDMADE`(수제버거). 한글 태그 라벨에 포함되는 검색어나 enum 이름으로도 리뷰를 찾을 수 있다. 태그는 작성자의 평가이며 재료 사실 정보가 아니다.

현재 LIKE는 입력 문자열의 포함 여부를 검사한다. 자연어 문장의 의도·동의어·유사 메뉴·재료 제외는 해석하지 않는다. 예를 들어 “피클 없는”을 문자열 검색하는 것은 피클이 없음을 보장하지 않는다. 이 한계를 정답 질의와 함께 측정한 뒤 분석기·검색 모델·재료 구조를 개선한다.

## 로컬 실행과 데이터

Java 21, MySQL 8.0, Redis 7.2 환경을 준비한다. 개발 환경은 별도 `local` 설정을 참조하므로 없으면 include를 비운다.

```sh
export DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=amumal DB_USERNAME=amumal
export DB_PASSWORD='로컬 DB 비밀번호'
export REDIS_HOST=127.0.0.1 REDIS_PORT=6379 REDIS_SSL_ENABLED=false
SPRING_PROFILES_INCLUDE= ./gradlew bootRun
```

시작 시 Flyway V6까지 적용한다. 브랜드·메뉴 관리 CRUD는 만들지 않았고 운영 데이터는 출처를 검증한 후 별도 SQL/데이터 적재 과정으로 넣는다. 빠른 개발 확인에는 아래 합성 데이터 3건을 사용할 수 있다. 자동으로 시드하지 않으며, 실행 대상 DB를 확인한 뒤 한 번 넣는다.

```sh
mysql -h 127.0.0.1 -u amumal -p amumal < scripts/seed-burger-demo.sql
```

`GET /v1/menus`에서 실제 발급된 ID를 확인하고 리뷰에 사용한다. 합성 데이터는 검색 실험용 실측 자료나 실제 메뉴로 간주하지 않는다.

## 최적화 이전의 기능 추가 기준선

기존 작성자 fetch join과 커서 페이지네이션만 유지한다. 새 메뉴·브랜드·태그는 기본 지연 조회를 사용하며, fetch join·batch fetch·검색용 복합 인덱스를 추가하지 않는다. SQL 실행 횟수를 줄이는 목표나 성능 개선 주장은 없다. 데이터 규모를 늘리고 실제 쿼리 수와 지연을 측정한 뒤 최적화는 별도 변경으로 진행한다.

기존 인증 경로 문제와 댓글·좋아요·이미지 매핑이 있는 게시글의 삭제 제약 문제도 기존 상태로 남긴다. 새 도메인 필드의 유효성 검사와 정상 조회를 위한 변경만 포함한다.

## 검증과 다음 단계

```sh
./gradlew test spotlessCheck bootJar
```

테스트는 기존 커서 페이지 누락·중복과 조회수 flush를 유지하며 메뉴 필터·평점·태그 갱신·서비스 계층 작성자 권한·통합 검색·기존 행 보존을 검증한다. 리뷰 HTTP 쓰기 테스트는 사용자 ID가 이미 해석된 요청으로 새 필드를 확인하며 실제 로그인 인증 흐름을 검증하지 않는다. 기본은 MySQL 모드 H2이며 실제 MySQL에서도 별도로 실행한다.

다음 순서는 메뉴 출처 수집/정제 → 재현 가능한 합성 리뷰와 정답 질의 구축 → LIKE 실행 계획과 p95/p99·RPS 측정 → 같은 조건의 Elasticsearch 비교 → Nori/BM25 품질 튜닝 → 동기화 장애 실험이다. 측정 전 성능 개선 수치를 주장하지 않는다.

배포와 서버 켜기/끄기는 [DEPLOYMENT.md](DEPLOYMENT.md)를 참고한다. 이 도메인 변경을 AWS에 배포하려면 별도 푸시·배포가 필요하며, 중지된 서버를 켜면 배포 전까지 기존 이미지가 실행된다.
