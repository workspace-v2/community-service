#!/usr/bin/env bash
# 배포 전 EC2 가용 메모리를 SSM으로 확인하고, 기준치(MIN_AVAILABLE_MB) 미만이면 잡을 실패시켜 배포를 막는다.
set -euo pipefail

# 환경변수 체크
: "${INSTANCE_ID:?INSTANCE_ID가 설정되지 않았습니다.}"
: "${MIN_AVAILABLE_MB:?MIN_AVAILABLE_MB가 설정되지 않았습니다.}"

# EC2에 "free -m 결과 중 MemAvailable 값만 출력해줘"라는 커맨드를 SSM으로 보내고, ssm 요청 id 값을 받아온다
# 명령어 전달한지 60초 지나면 DeliveryTimedOut 처리
CID=$(aws ssm send-command \
  --instance-ids "$INSTANCE_ID" \
  --document-name "AWS-RunShellScript" \
  --timeout-seconds 60 \
  --parameters 'commands=["awk '\''/^MemAvailable:/ {printf \"%d\\n\", $2 / 1024}'\'' /proc/meminfo"]' \
  --query "Command.CommandId" \
  --output text)

# send-command 자체는 비동기로 요청 후 command id만 바로 반환받아야 정상 -> 못받으면 exit 1
if [[ -z "$CID" || "$CID" == "None" ]]; then
  echo "SSM Command ID를 받지 못했습니다." >&2
  exit 1
fi

echo "SSM 메모리 확인 명령 전송 완료: $CID"

# SSM 명령이 성공할 때까지 대기, 대기 성공 시 아래 if 조건을 건너 뛴다
if ! aws ssm wait command-executed \
  --command-id "$CID" \
  --instance-id "$INSTANCE_ID"; then

  # 실패 상태 조회
  if ! STATUS=$(aws ssm get-command-invocation \
    --command-id "$CID" \
    --instance-id "$INSTANCE_ID" \
    --query "StatusDetails" \
    --output text 2>/dev/null); then
    STATUS="상태 조회 실패"
  fi

  # 표준 오류 조회
  if ! STDERR=$(aws ssm get-command-invocation \
    --command-id "$CID" \
    --instance-id "$INSTANCE_ID" \
    --query "StandardErrorContent" \
    --output text 2>/dev/null); then
    STDERR="오류 로그 조회 실패"
  fi

  echo "SSM 명령 실행 실패: $STATUS" >&2

  if [[ -n "$STDERR" && "$STDERR" != "None" ]]; then
    echo "$STDERR" >&2
  fi

  exit 1
fi

# 실행 결과 조회
AVAILABLE_MB=$(aws ssm get-command-invocation \
  --command-id "$CID" \
  --instance-id "$INSTANCE_ID" \
  --query "StandardOutputContent" \
  --output text | tr -d '[:space:]')

# 결과값 검증 - 숫자로만 이루어진 값인지 검사
if ! [[ "$AVAILABLE_MB" =~ ^[0-9]+$ ]]; then
  echo "가용 메모리 조회 결과가 올바르지 않습니다: '$AVAILABLE_MB'" >&2
  exit 1
fi

# 결과값 디버깅용 로그
echo "available: ${AVAILABLE_MB}MiB (min: ${MIN_AVAILABLE_MB}MiB)"

# 가용 메모리 기준 검사
if (( AVAILABLE_MB < MIN_AVAILABLE_MB )); then
  echo "가용 메모리 부족, 배포 중단" >&2
  exit 1
fi

echo "가용 메모리 검사 통과"