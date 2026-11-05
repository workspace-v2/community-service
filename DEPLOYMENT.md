# Burgerpedia 배포

main/dev 브랜치는 GitHub Actions → ECR → ECS Fargate/CodeDeploy 블루·그린으로 배포합니다.
기존 워크플로는 job 조건으로 비활성화했고 파일은 보존했습니다.
도메인 전환은 아직 하지 않았으며 기존 게시글 API를 그대로 사용합니다.

- main: https://main-burger.pattie.cloud
- dev: https://dev-burger.pattie.cloud
- 새 워크플로: `.github/workflows/burger-ci-cd.yml`
- 배포용 이미지: `Dockerfile.ecs` (테스트를 통과한 bootJar 필요)
- 앱 포트: 3000, 내부 management 포트: 8081
- 프로필: `production,ecs`
- 배포 스위치: repository variable `BURGER_DEPLOY_ENABLED=true`

인프라 최초 구성·공통 모니터링·부하기·전원 스크립트는 같은 workspace의
`../terraform/BURGER.md`를 참고합니다. 인프라 구성이 끝나기 전에는 CI 검증만 실행됩니다.
브랜치별 AWS OIDC 역할은 BURGER_AWS_ROLE_ARN_MAIN / BURGER_AWS_ROLE_ARN_DEV로 설정합니다.
BURGER_URL_MAIN / BURGER_URL_DEV / BURGER_AWS_REGION도 Terraform 스크립트가 설정합니다.

전원 스크립트로 꺼 둔 환경은 CI/CD가 자동으로 켜지 않습니다.
블루·그린 배포 중에도 DB는 공유하므로 마이그레이션은 이전/새 버전과 모두 호환돼야 합니다.

## 버거 리뷰 전환 이후

게시글 API는 `/v1/posts`를 유지하고 menuId·rating·tags를 사용한다. 메뉴/리뷰 통합 검색은 `/v1/search`다. 배포 시 V6가 실행되며 기존 종목 데이터는 보존하고 버거 검색에서 제외한다. 상세 계약과 개발용 합성 카탈로그 적재는 README를 참고한다. 이 서비스는 더 이상 market-data-service를 호출하지 않는다.

전체 서버 중지에는 `burger-power.sh all stop` 외에 공통 `env-power.sh monitoring stop`, `env-power.sh load-generator stop`도 필요하다. 중지 시 ALB와 저장 데이터는 유지되므로 해당 리소스 요금은 남는다.
