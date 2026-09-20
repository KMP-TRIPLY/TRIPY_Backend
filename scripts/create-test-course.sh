#!/usr/bin/env bash
#
# 현장에 안 가고도 게임 한 판을 끝까지 돌려보기 위한 테스트 코스를 만든다.
# 스팟 둘을 120m 안에 붙여 놓고 반경을 넉넉히 줘서, 한자리에 서서 도착 인증까지 된다.
# 충남 코스와 같은 구성(퀴즈 + 사진 인증 + 선택형)이라 판정 경로를 전부 지나간다.
#
#   BASE_URL=https://example.com TOKEN=<accessToken> bash scripts/create-test-course.sh
#
# 기본 좌표는 의왕시청 부근이다. 정확한 자리에서 테스트하려면 지금 있는 곳의 좌표를 넣는다.
#   LAT=37.3449 LNG=126.9683 ... bash scripts/create-test-course.sh
#
# 앱에서 충남(44) 목록으로 보고 싶으면 REGION_CODE=44 를 준다. 비워두면 도시명으로 정해진다(의왕 -> 41).
set -euo pipefail

BASE_URL="${BASE_URL:?BASE_URL 이 필요합니다 (예: https://tripy.example.com)}"
TOKEN="${TOKEN:?로그인 후 받은 accessToken 이 필요합니다}"
LAT="${LAT:-37.3449}"
LNG="${LNG:-126.9683}"
CITY="${CITY:-의왕}"
REGION_CODE="${REGION_CODE:-}"
RADIUS="${RADIUS:-300}"

# 두 번째 스팟은 북쪽으로 약 120m. 반경 안이라 제자리에서도 도착 인증이 된다.
LAT2=$(awk "BEGIN{printf \"%.6f\", $LAT + 0.0011}")

post() { # path json -> 응답 본문
  curl -sS -X POST "$BASE_URL$1" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json; charset=utf-8" \
    -d "$2"
}

# 공백이 섞여 있어도 걸리게 한다. 응답에서 처음 나오는 id 가 방금 만든 것의 id 다.
first_id() { grep -o '"id"[[:space:]]*:[[:space:]]*[0-9][0-9]*' | head -1 | grep -o '[0-9][0-9]*$'; }

die() { echo "실패: $1" >&2; echo "$2" >&2; exit 1; }

region_field=""
if [ -n "$REGION_CODE" ]; then
  region_field="\"regionCode\":\"$REGION_CODE\","
fi

echo "코스를 만듭니다... (기준 좌표 $LAT, $LNG / 반경 ${RADIUS}m)"
course_body=$(cat <<JSON
{
  "title": "[테스트] $CITY 에서 도는 한자리 코스",
  "description": "개발 확인용 코스입니다. 스팟 둘이 120m 안에 붙어 있어 한자리에서 끝까지 진행됩니다.",
  $region_field
  "city": "$CITY",
  "difficulty": "EASY",
  "estimatedMinutes": 20,
  "courseType": "GENERAL",
  "indoorType": "MIXED",
  "tags": ["PHOTO_SPOT"]
}
JSON
)
course_res=$(post "/api/courses" "$course_body")
COURSE_ID=$(printf '%s' "$course_res" | first_id)
[ -n "$COURSE_ID" ] || die "코스 생성" "$course_res"
echo "  코스 id=$COURSE_ID"

add_spot() { # 순서 이름 위도 경도 이야기
  local body
  body=$(cat <<JSON
{
  "newTourismSpot": { "name": "$2", "lat": $3, "lng": $4, "address": "$CITY" },
  "sequenceOrder": $1,
  "storyText": "$5",
  "lat": $3,
  "lng": $4,
  "radiusMeters": $RADIUS,
  "indoor": false
}
JSON
)
  post "/api/courses/$COURSE_ID/spots" "$body"
}

add_mission() { # 스팟id json
  post "/api/courses/$COURSE_ID/spots/$1/missions" "$2"
}

spot_res=$(add_spot 1 "테스트 스팟 1" "$LAT" "$LNG" "첫 번째 지점입니다. 퀴즈를 풀고 사진을 남겨주세요.")
SPOT1=$(printf '%s' "$spot_res" | first_id)
[ -n "$SPOT1" ] || die "스팟 1 생성" "$spot_res"
echo "  스팟 1 id=$SPOT1 ($LAT, $LNG)"

spot_res=$(add_spot 2 "테스트 스팟 2" "$LAT2" "$LNG" "두 번째 지점입니다. 마지막 인증샷을 남기면 끝납니다.")
SPOT2=$(printf '%s' "$spot_res" | first_id)
[ -n "$SPOT2" ] || die "스팟 2 생성" "$spot_res"
echo "  스팟 2 id=$SPOT2 ($LAT2, $LNG)"

add_mission "$SPOT1" '{
  "missionType": "QUIZ_TEXT",
  "question": "이 코스는 무엇을 확인하려고 만든 코스일까요? (답: 테스트)",
  "answer": "테스트",
  "hint": "질문 안에 답이 있습니다",
  "hintPenalty": 150,
  "baseScore": 300
}' > /dev/null
echo "  스팟 1 - 주관식 퀴즈 추가"

add_mission "$SPOT1" '{
  "missionType": "PHOTO",
  "question": "지금 서 있는 곳에서 팀 전원 인증샷",
  "baseScore": 100
}' > /dev/null
echo "  스팟 1 - 사진 인증 추가 (사람이 찍혀야 통과합니다)"

add_mission "$SPOT2" '{
  "missionType": "QUIZ_CHOICE",
  "question": "이 스팟에서 다음에 할 일은?",
  "choices": [
    { "label": "사진을 찍는다", "value": "photo", "correct": true },
    { "label": "그냥 지나간다", "value": "pass", "correct": false }
  ],
  "baseScore": 300
}' > /dev/null
echo "  스팟 2 - 선택형 퀴즈 추가"

add_mission "$SPOT2" '{
  "missionType": "PHOTO",
  "question": "주변 건물이나 간판이 보이도록 사진을 찍어라",
  "baseScore": 100
}' > /dev/null
echo "  스팟 2 - 사진 인증 추가 (대상이 보여야 통과합니다)"

echo
echo "완료. courseId=$COURSE_ID 로 방을 만들면 됩니다."
echo "  도착 인증에 보낼 좌표: lat=$LAT lng=$LNG (스팟 2 는 lat=$LAT2)"
