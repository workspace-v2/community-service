# StockTalk

주식 종목별 게시글과 댓글을 공유하는 주식 커뮤니티 서비스입니다.

---

### 목차

- [Tech Stack](#tech-stack)
- [주요 기능](#주요-기능)
    - [1. 로그인 · 회원가입](#1-로그인--회원가입)
    - [2. 게시글 목록 · 검색](#2-게시글-목록--검색)
    - [3. 게시글 작성](#3-게시글-작성)
    - [4. 게시글 상세 및 댓글](#4-게시글-상세-및-댓글)
    - [5. 마이페이지](#5-마이페이지)
- [Architecture](#architecture)
- [GitOps 기반 배포 자동화](#gitops-기반-배포-자동화)
- [ERD](#erd)

---

### Tech Stack

- Backend  
  ![Java](https://img.shields.io/badge/Java_21-007396?style=flat-square&logo=java&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring&logoColor=white)

- Storage  
  ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white) ![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazons3&logoColor=white)

- Infra  
  ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white) ![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat-square&logo=kubernetes&logoColor=white) ![Helm](https://img.shields.io/badge/Helm-0F1689?style=flat-square&logo=helm&logoColor=white) ![ArgoCD](https://img.shields.io/badge/ArgoCD-EF7B4D?style=flat-square&logo=argo&logoColor=white) ![Traefik](https://img.shields.io/badge/Traefik-24A1C1?style=flat-square&logo=traefikproxy&logoColor=white) ![AWS](https://img.shields.io/badge/AWS-232F3E?style=flat-square&logo=amazonwebservices&logoColor=white)

- Monitoring  
  ![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=flat-square&logo=prometheus&logoColor=white) ![Grafana](https://img.shields.io/badge/Grafana-F46800?style=flat-square&logo=grafana&logoColor=white) ![Loki](https://img.shields.io/badge/Loki-F46800?style=flat-square&logo=grafana&logoColor=white) ![Vector](https://img.shields.io/badge/Vector-000000?style=flat-square)

- CI/CD  
  ![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)

### 주요 기능

### 1. 로그인 · 회원가입

JWT 액세스/리프레시 토큰을 발급하고, HttpOnly 쿠키로 안전하게 보관해 인증 상태를 유지합니다.

<p align="center">
  <img src="" alt="로그인 및 회원가입 데모" width="80%" max-width="700px" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

<br/>

### 2. 게시글 목록 · 검색

종목별로 게시글 목록을 무한 스크롤로 이어서 확인할 수 있으며,  
키워드 검색으로 원하는 종목이나 주제의 게시글만 찾아볼 수 있습니다.

<p align="center">
  <img src="" alt="게시글 목록 및 검색 데모" width="80%" max-width="700px" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

<br/>

### 3. 게시글 작성

관심 있는 종목을 선택하고 제목·본문과 이미지를 작성해 게시글을 등록할 수 있습니다.

<p align="center">
  <img src="" alt="게시글 작성 데모" width="80%" max-width="700px" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

<br/>

### 4. 게시글 상세 및 댓글

게시글 본문과 첨부 이미지를 확인하고, 댓글을 작성·수정·삭제하거나 좋아요를 남길 수 있습니다.

<p align="center">
  <img src="" alt="게시글 상세 및 댓글 데모" width="80%" max-width="700px" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

<br/>

### 5. 마이페이지

내가 작성한 게시글을 모아보고, 닉네임·프로필 이미지·비밀번호 등 계정 정보를 한 곳에서 관리할 수 있습니다.

<p align="center">
  <img src="" alt="마이페이지 데모" width="80%" max-width="700px" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

---

### Architecture

요청 흐름: `Route 53 → ALB → Nginx → Traefik → Kubernetes Service → Pod`

<p align="center">
  <img src="https://github.com/user-attachments/assets/5f1c2dbc-b1ed-447f-9d56-bd2e7e08d297" alt="Architecture" width="100%" max-width="900px" />
</p>

---

### GitOps 기반 배포 자동화

`Push / PR` ➔ `CI (빌드 및 테스트)` ➔ `Docker 이미지 빌드 & GHCR 푸시` ➔ `GitOps Repo 갱신` ➔ `ArgoCD 자동 배포` 순으로 진행됩니다.

| Workflow               | Trigger                              | 수행 역할                                               |
|:-----------------------|:-------------------------------------|:----------------------------------------------------|
| `ci.yml`               | `main`, `dev` Push <br> `main` 대상 PR | 애플리케이션 빌드 및 자동화 테스트 (CI)                            |
| `publish-image.yml`    | 수동 실행 (Workflow Dispatch)            | Docker 이미지 빌드 후 GitHub Container Registry(GHCR)에 푸시 |
| `update-gitops.yml`    | `publish-image.yml` 성공 시             | GitOps 레포지토리(`k8s-pilot`)의 Helm values 이미지 태그 갱신    |
| **ArgoCD Sync**        | GitOps 레포지토리 변경 감지                   | 변경된 매니페스트를 K8s 클러스터에 자동 동기화 및 배포 (CD)               |
| `prune-ghcr-image.yml` | 매주 일요일 03:21 (KST)                   | 미태그 이미지 삭제 및 최신 30개 태그 유지 (스토리지 최적화)                |

---

### ERD

<p align="center">
  <img src="https://github.com/user-attachments/assets/07b32ded-658f-4632-b35b-383f826f07b7" alt="Architecture" width="100%" max-width="900px" />
</p>


### 로컬 데이터베이스

MySQL 8.0을 사용합니다. `amumal` 데이터베이스와 해당 DB에 접근할 사용자를 준비하고,
다음 환경변수를 설정합니다.

```sh
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=amumal
export DB_USERNAME=amumal
export DB_PASSWORD='your-local-password'
```

기본 문자셋은 `utf8mb4`를 권장합니다. Flyway가 빈 데이터베이스에 스키마를 생성합니다.
개발 프로필은 `application-local.yml`을 포함하므로 로컬 설정 파일도 준비해야 합니다.
Redis 등 나머지 연결 설정은 실행 환경에 맞게 지정합니다.

기본 테스트는 H2의 MySQL 호환 모드로 실행합니다. CI에서는 실제 MySQL에 대해서도
동일한 테스트를 실행해 마이그레이션과 JPA 스키마 검증을 확인합니다.
이 저장소의 마이그레이션은 새 MySQL 데이터베이스를 위한 초기 구성입니다.
기존 데이터베이스의 데이터 이관은 별도 작업이 필요합니다.
