# StockTalk

주식 종목별 게시글과 댓글을 공유하는 주식 커뮤니티 서비스입니다.

---

## 주요 기능

### 로그인 · 회원가입

* JWT 액세스/리프레시 토큰을 발급하고, HttpOnly 쿠키로 안전하게 보관해 인증 상태를 유지합니다.

<p align="center">
  <img src="" alt="로그인 및 회원가입 데모" width="80%" max-width="700px" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

<br/>

### 게시글 목록 · 검색

* 종목별로 게시글 목록을 모아보고, 무한 스크롤로 이어서 확인할 수 있습니다.
* 키워드 검색으로 원하는 종목이나 주제의 게시글만 찾아볼 수 있습니다.

<p align="center">
  <img src="" alt="게시글 목록 및 검색 데모" width="80%" max-width="700px" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

<br/>

### 게시글 작성

* 관심 있는 종목을 선택하고 제목·본문과 이미지를 작성해 게시글을 등록할 수 있습니다.

<p align="center">
  <img src="" alt="게시글 작성 데모" width="80%" max-width="700px" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

<br/>

### 게시글 상세 및 댓글

* 게시글 본문과 첨부 이미지를 확인하고, 댓글을 작성·수정·삭제하거나 좋아요를 남길 수 있습니다.

<p align="center">
  <img src="" alt="게시글 상세 및 댓글 데모" width="80%" max-width="700px" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

<br/>

### 마이페이지

* 내가 작성한 게시글을 모아보고, 닉네임·프로필 이미지·비밀번호 등 계정 정보를 한 곳에서 관리할 수 있습니다.

<p align="center">
  <img src="" alt="마이페이지 데모" width="80%" max-width="700px" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

## Architecture

요청 흐름: `Route 53 → ALB → Nginx → Traefik → Kubernetes Service → Pod`

<p align="center">
  <img src="https://github.com/user-attachments/assets/5f1c2dbc-b1ed-447f-9d56-bd2e7e08d297" alt="Architecture" width="100%" max-width="900px" />
</p>