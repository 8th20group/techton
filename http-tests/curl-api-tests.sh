#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
GITHUB_ID="if$(date +%s)"
NICKNAME="이프"
TODAY="$(date +%F)"
MISSION_IMAGE="/tmp/techton-mission-sample.txt"

extract_number() {
  echo "$1" | sed -n "s/.*\"$2\":\([0-9][0-9]*\).*/\1/p"
}

request() {
  local title="$1"
  shift

  echo
  echo "### $title"
  curl -s "$@"
  echo
}

echo "BASE_URL=$BASE_URL"
echo "GITHUB_ID=$GITHUB_ID"
echo "TODAY=$TODAY"

echo "mission sample image" > "$MISSION_IMAGE"

CREATE_RESPONSE="$(request "크루 회원가입" \
  -X POST "$BASE_URL/crews" \
  -H "Content-Type: application/json" \
  -d "{
    \"githubId\": \"$GITHUB_ID\",
    \"nickname\": \"$NICKNAME\",
    \"track\": \"BE\"
  }")"
echo "$CREATE_RESPONSE"
CREW_ID="$(extract_number "$CREATE_RESPONSE" "id")"

if [ -z "$CREW_ID" ]; then
  echo "crew id를 응답에서 찾지 못했습니다."
  exit 1
fi

request "로그인" \
  -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"githubId\": \"$GITHUB_ID\"
  }"

request "내 정보 확인" \
  "$BASE_URL/auth/me?githubId=$GITHUB_ID"

request "커밋 인증 - 최초 5P 지급" \
  -X POST "$BASE_URL/crews/$CREW_ID/activities/commit" \
  -H "Content-Type: application/json" \
  -d "{
    \"activityDate\": \"$TODAY\",
    \"githubUrl\": \"https://github.com/example/repo/commit/abc\"
  }"

request "커밋 인증 - 같은 날짜 중복 0P" \
  -X POST "$BASE_URL/crews/$CREW_ID/activities/commit" \
  -H "Content-Type: application/json" \
  -d "{
    \"activityDate\": \"$TODAY\",
    \"githubUrl\": \"https://github.com/example/repo/commit/def\"
  }"

request "리뷰 인증 - 최초 5P 지급" \
  -X POST "$BASE_URL/crews/$CREW_ID/activities/review" \
  -H "Content-Type: application/json" \
  -d "{
    \"activityDate\": \"$TODAY\",
    \"reviewUrl\": \"https://github.com/example/repo/pull/1#discussion_r123\"
  }"

MISSION_RESPONSE="$(request "미션 인증 요청 - 사진 업로드, PENDING" \
  -X POST "$BASE_URL/crews/$CREW_ID/activities/mission" \
  -F "activityDate=$TODAY" \
  -F "memo=LMS 최종 단계 완료했습니다." \
  -F "image=@$MISSION_IMAGE")"
echo "$MISSION_RESPONSE"
MISSION_ACTIVITY_ID="$(extract_number "$MISSION_RESPONSE" "activityId")"

BLOG_RESPONSE="$(request "블로그 인증 요청 - 링크 제출, PENDING" \
  -X POST "$BASE_URL/crews/$CREW_ID/activities/blog" \
  -H "Content-Type: application/json" \
  -d "{
    \"activityDate\": \"$TODAY\",
    \"blogUrl\": \"https://velog.io/@if/post\",
    \"memo\": \"이번 미션 회고입니다.\"
  }")"
echo "$BLOG_RESPONSE"
BLOG_ACTIVITY_ID="$(extract_number "$BLOG_RESPONSE" "activityId")"

request "검수 대기 목록 조회" \
  "$BASE_URL/admin/activities/pending"

if [ -n "$MISSION_ACTIVITY_ID" ]; then
  request "미션 인증 승인 - 10P 지급" \
    -X PATCH "$BASE_URL/admin/activities/$MISSION_ACTIVITY_ID/approve"
fi

if [ -n "$BLOG_ACTIVITY_ID" ]; then
  request "블로그 인증 거절" \
    -X PATCH "$BASE_URL/admin/activities/$BLOG_ACTIVITY_ID/reject" \
    -H "Content-Type: application/json" \
    -d "{
      \"reason\": \"블로그 링크 확인 실패\"
    }"
fi

request "주간 포인트 체크리스트" \
  "$BASE_URL/crews/$CREW_ID/weekly-activities"

request "포인트 요약" \
  "$BASE_URL/crews/$CREW_ID/points/summary"

request "포인트 내역" \
  "$BASE_URL/crews/$CREW_ID/point-histories"

request "크루 랭킹" \
  "$BASE_URL/rankings/crews"

request "코치 랭킹" \
  "$BASE_URL/rankings/coaches"
