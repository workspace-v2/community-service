# EC2(원격 서버)에서 실행되는 배포 스크립트, 클라우드 무관(Docker만 있으면 동작) 로직
set -e

# 필수값 세팅 및 사전 검증
IMAGE_REPO='${IMAGE_REPO}'
COMMIT_HASH_VALUE='${COMMIT_HASH_VALUE}'
DB_HOST='${DB_HOST}'
DB_PORT='${DB_PORT}'
DB_NAME='${DB_NAME}'
DB_USERNAME='${DB_USERNAME}'
DB_PASSWORD='${DB_PASSWORD}'
SERVER_PORT='${PORT}'
GHCR_TOKEN='${GHCR_TOKEN}'
GHCR_ACTOR='${GITHUB_ACTOR}'

: "${IMAGE_REPO:?IMAGE_REPO가 설정되지 않았습니다.}"
: "${COMMIT_HASH_VALUE:?COMMIT_HASH_VALUE가 설정되지 않았습니다.}"
: "${DB_HOST:?DB_HOST가 설정되지 않았습니다.}"
: "${DB_PORT:?DB_PORT가 설정되지 않았습니다.}"
: "${DB_NAME:?DB_NAME가 설정되지 않았습니다.}"
: "${DB_USERNAME:?DB_USERNAME가 설정되지 않았습니다.}"
: "${DB_PASSWORD:?DB_PASSWORD가 설정되지 않았습니다.}"
: "${SERVER_PORT:?SERVER_PORT 설정되지 않았습니다.}"
: "${GHCR_TOKEN:?GHCR_TOKEN이 설정되지 않았습니다.}"
: "${GHCR_ACTOR:?GHCR_ACTOR가 설정되지 않았습니다.}"

# GHCR 로그인
echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_ACTOR" --password-stdin

docker pull ${IMAGE_REPO}:${COMMIT_HASH_VALUE}

# 롤백 대비 - 현재 떠 있는 컨테이너가 어떤 이미지인지 미리 기억해두기
# (최초 배포라 기존 컨테이너가 없으면 PREV_IMAGE는 빈 값으로 유지)
PREV_IMAGE=""
# name 필터는 부분 일치라 뒤이은 inspect/stop/rm(정확히 "amumal")과 타겟 대상이 있으니 일치 시키기
if [ -n "$(docker ps -aq -f name='^/amumal$')" ]; then
  PREV_IMAGE=$(docker inspect --format='{{.Config.Image}}' amumal)
  docker stop amumal
  docker rm amumal
fi

# 새 컨테이너 실행
docker run -d --name amumal --restart unless-stopped -p "${SERVER_PORT}":"${SERVER_PORT}" \
  -e DB_HOST='${DB_HOST}' \
  -e DB_PORT='${DB_PORT}' \
  -e DB_NAME='${DB_NAME}' \
  -e DB_USERNAME='${DB_USERNAME}' \
  -e DB_PASSWORD='${DB_PASSWORD}' \
  ${IMAGE_REPO}:${COMMIT_HASH_VALUE}

# 새 컨테이너가 진짜 떴는지 헬스 체크 (최대 30초 동안)
# TODO: 컨테이너 뜨는데 보통 얼마나 걸리는지 확인 후 '30초' 값 수정
HEALTHY=false
for i in $(seq 1 10); do
  if curl -sf "http://localhost:${SERVER_PORT}/v1/health" > /dev/null; then
    HEALTHY=true
    break
  fi
  sleep 3
done

# 헬스 체크 실패한 경우
if [ "$HEALTHY" == "false" ]; then
  echo "헬스체크 실패, 롤백 진행"
  docker logs amumal --tail 50
  docker stop amumal
  docker rm amumal

  # 이전 이미지가 존재하면 해당 이미지로 다시 배포 (최초 배포라 이전 이미지가 없으면 롤백 불가 메세지 출력)
  if [ -n "$PREV_IMAGE" ]; then
    docker run -d --name amumal --restart unless-stopped -p "${SERVER_PORT}":"${SERVER_PORT}" \
      -e DB_HOST='${DB_HOST}' \
      -e DB_PORT='${DB_PORT}' \
      -e DB_NAME='${DB_NAME}' \
      -e DB_USERNAME='${DB_USERNAME}' \
      -e DB_PASSWORD='${DB_PASSWORD}' \
      "$PREV_IMAGE"
    echo "이전 이미지($PREV_IMAGE)로 롤백 완료"
  else
    echo "이전 이미지 정보 없음, 롤백 불가"
  fi

  exit 1
fi

docker image prune -f
