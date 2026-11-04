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
