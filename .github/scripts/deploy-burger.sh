#!/usr/bin/env bash
set -euo pipefail
export AWS_PAGER=""
export AWS_RETRY_MODE=standard
export AWS_MAX_ATTEMPTS=5
: "${IMAGE:?}" "${TASK_FAMILY:?}" "${ECS_CLUSTER:?}" "${ECS_SERVICE:?}"
: "${CODEDEPLOY_APPLICATION:?}" "${CODEDEPLOY_GROUP:?}"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

# Use the latest Terraform task configuration, then replace only the app image.
aws ecs describe-task-definition --task-definition "$TASK_FAMILY" --query taskDefinition > "$work/base.json"
jq --arg image "$IMAGE" '
  .containerDefinitions |= map(if .name == "app" then .image = $image else . end)
  | del(.taskDefinitionArn, .revision, .status, .requiresAttributes, .compatibilities,
        .registeredAt, .registeredBy, .deregisteredAt)
' "$work/base.json" > "$work/task.json"
arn=$(aws ecs register-task-definition --cli-input-json "file://$work/task.json" --query taskDefinition.taskDefinitionArn --output text)

# Inline AppSpec avoids storing an application revision in a publicly readable bucket.
jq -n --arg arn "$arn" '{
  version: 0.0,
  Resources: [{TargetService: {Type: "AWS::ECS::Service", Properties: {
    TaskDefinition: $arn, LoadBalancerInfo: {ContainerName: "app", ContainerPort: 3000},
    PlatformVersion: "1.4.0"
  }}}]
}' > "$work/appspec.json"
jq -n --arg app "$CODEDEPLOY_APPLICATION" --arg group "$CODEDEPLOY_GROUP" \
  --arg content "$(cat "$work/appspec.json")" '{
  applicationName: $app, deploymentGroupName: $group,
  revision: {revisionType: "AppSpecContent", appSpecContent: {content: $content}}
}' > "$work/deployment.json"
# Re-check after the image build: a user may have stopped the test environment meanwhile.
aws ecs describe-services --cluster "$ECS_CLUSTER" --services "$ECS_SERVICE" > "$work/service.json"
jq -e '.failures == [] and (.services | length == 1) and (.services[0].desiredCount > 0)' "$work/service.json" >/dev/null || {
  echo 'Environment is stopped; deployment refused.' >&2; exit 1
}
id=$(aws deploy create-deployment --cli-input-json "file://$work/deployment.json" --query deploymentId --output text)
echo "CodeDeploy deployment: $id"
# The CLI waiter times out before some deployments finish. Poll with an explicit 30m limit.
for _ in $(seq 1 180); do
  if ! status=$(aws deploy get-deployment --deployment-id "$id" --query deploymentInfo.status --output text); then
    echo "Temporary deployment status lookup failure; retrying deployment $id." >&2
    sleep 10
    continue
  fi
  case "$status" in
    Succeeded)
      summary=$(printf '%s\n' \
        "### Burgerpedia ${DEPLOY_ENV:-} deployment succeeded" \
        "- Deployment: $id" "- Task definition: $arn" "- Image: $IMAGE")
      if [ -n "${GITHUB_STEP_SUMMARY:-}" ]; then
        printf '%s\n' "$summary" >> "$GITHUB_STEP_SUMMARY"
      else
        printf '%s\n' "$summary"
      fi
      exit 0 ;;
    Failed|Stopped)
      aws deploy get-deployment --deployment-id "$id" --query 'deploymentInfo.{status:status,error:errorInformation,rollback:rollbackInfo}'
      exit 1 ;;
  esac
  sleep 10
done
echo "Deployment $id still running after 30 minutes. Inspect CodeDeploy before another deploy or power operation." >&2
exit 1
