정리

# 우테코 해커톤 - 크루 포인트 상점

커밋, 리뷰, 미션 완료, 회고/블로그 작성 등 우테코 활동을 통해 포인트를 획득하고, 획득한 포인트로 랜덤박스와 코치 이용권을 구매하는 해커톤 서비스입니다.

해커톤 MVP에서는 보안과 자동화보다 빠른 개발과 시연 가능성을 우선합니다.

---

## 0. 프론트 연동 빠른 확인

### 서버 실행

```bash
./gradlew bootRun
```

기본 주소는 `http://localhost:8080` 입니다.

### Swagger UI

브라우저에서 아래 주소로 API를 확인하고 직접 호출할 수 있습니다.

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON은 아래 주소에서 확인할 수 있습니다.

```text
http://localhost:8080/v3/api-docs
```

### HTTP 요청 파일

IntelliJ HTTP Client 기준으로 아래 파일들을 사용할 수 있습니다.

| 파일 | 내용 |
|---|---|
| `http/auth.http` | 로그인, 내 정보 |
| `http/point.http` | 포인트 내역 |
| `http/github-sync.http` | GitHub 활동 수동 동기화 |
| `http/shop.http` | 상점, 랜덤박스, 코치 이용권 구매 |
| `http/ticket.http` | 이용권 목록, 사용 처리 |
| `http/coach.http` | 코치 목록 |
| `http/ranking.http` | 크루/코치 랭킹 |

### 프론트 기본 흐름

1. `POST /auth/login`으로 로그인하고 응답의 `crewId`, `githubId`를 `localStorage`에 저장합니다.
2. 새로고침 시 `GET /auth/me?githubId={githubId}`로 로그인 상태를 복원합니다.
3. 메인 화면은 `GET /crews/{crewId}/points/summary`, `GET /rankings/crews`, `GET /rankings/coaches`를 조합합니다.
4. 체크리스트 화면은 `GET /crews/{crewId}/weekly-activities`를 사용합니다.
5. 초기 화면이나 체크리스트 화면에서 `POST /crews/{crewId}/activities/github/sync`를 호출하면 오늘 GitHub 활동을 즉시 불러올 수 있습니다.
6. 미션 인증은 `multipart/form-data`로 `activityDate`, `image`, `memo`를 전송합니다.
7. 블로그 인증은 JSON으로 `activityDate`, `blogUrl`, `memo`를 전송합니다.

### 공통 에러 응답

현재 MVP는 대부분의 비즈니스 오류를 `400 Bad Request`로 내려줍니다.

```json
{
  "message": "포인트가 부족합니다."
}
```

동시 포인트 사용 충돌은 `409 Conflict`로 내려줄 수 있습니다.

---

## 1. 서비스 개요

### 핵심 아이디어

- 크루는 활동을 인증하고 포인트를 획득한다.
- 포인트는 주간 최대 100점까지 획득할 수 있다.
- 포인트로 랜덤박스 또는 코치 이용권을 구매할 수 있다.
- 코치 이용권은 코치 동의하에 사용할 수 있으며, 코치는 랜덤으로 배정된다.
- 미션 인증, 블로그 인증 등은 우선 수동 검수 방식으로 처리한다.
- 커밋과 리뷰는 GitHub public events를 매일 06:00에 조회해 자동 반영한다.
- 추후 LMS API, 크롤링, AI 검수 등으로 자동화 범위를 넓힐 수 있다.

---

## 2. 역할 분담

| 역할 | 담당자 |
|---|---|
| 포인트 정책 | 이프 |
| 상점 / 크루 인증 | 요크 |
| UI | 맥스 |

---

## 3. 기술 스택

### Backend

- Java 21
- Spring Boot
- Spring Data JPA
- H2 Database

### Frontend

- static/resources 에 CSR 기반으로 제작한다.
- Vanila Js
- HTML, CSS

### Database

- 해커톤 MVP: H2
- 추후 운영: MySQL 또는 PostgreSQL 전환 가능

---

## 4. 인증 정책

해커톤 MVP에서는 OAuth, JWT, Session을 사용하지 않습니다.

### 로그인 방식

- GitHub ID 기반 로그인
- 비밀번호 없음
- 보안 고려하지 않음
- 회원가입 없음
- 크루 데이터는 `members.json`, `fe_members.json`, `aos_members.json` 기반 `data.sql`로 서버 시작 시 미리 적재
- 로그인 시 GitHub ID는 대소문자를 구분하지 않음
- 프론트는 로그인 성공 후 `crewId` 또는 `githubId`를 localStorage에 저장
- 새로고침 시 `/auth/me` API로 로그인 상태 확인

### 크루 인증

- OAuth는 우선 제외
- 등록된 우테코 크루만 로그인 가능
- 등록 기준 정보
  - GitHub ID
  - 닉네임
  - 파트
- `AOS` 파트는 서버 `Track` enum의 `ANDROID`로 저장
- 기수는 8기로 고정 저장

---

## 5. 화면 구성

### 5.1 로그인 페이지

입력값:

- GitHub ID

MVP에서는 GitHub ID만으로 로그인합니다.
등록된 크루가 아니면 로그인할 수 없습니다.

---

### 5.2 메인 페이지

표시 정보:

- 보유 포인트
- 이번주 획득 포인트
- 주간 최대 획득 가능 포인트
- 커밋 / 리뷰 / 미션 / 블로그 활동별 포인트 현황
- 상점 이동 버튼
- 주간 포인트 체크리스트 이동 버튼
- 이번주 가장 많이 납치된 코치
- 크루별 랭킹

---

### 5.3 주간 포인트 체크리스트 페이지

표시 정보:

- 이번주 기간
- 이번주 획득 포인트
- 주간 최대 포인트
- 커밋 횟수 및 포인트
- 리뷰 횟수 및 포인트
- 미션 인증 여부 및 포인트
- 블로그 인증 여부 및 포인트

---

### 5.4 포인트 상점 페이지

상품:

- 랜덤 박스
- 코치 이용권 - 카페
- 코치 이용권 - 식사

코치 이용권은 구매 시 코치가 랜덤 배정됩니다.

---

## 6. 포인트 정책

### 주간 최대 획득 포인트

- 주간 최대 100P

| 활동 | 획득 포인트 | 제한 | 주간 최대 |
|---|---:|---|---:|
| 커밋 | 5P | 하루 1회 인정 | 35P |
| 리뷰 | 5P | 하루 1회 인정 | 35P |
| 미션 성공 | 10P | 검수 필요 | 10P |
| 회고 / 기술 블로그 | 20P | 검수 필요 | 20P |

### 커밋 정책

- 하루 1회만 인정
- 하루에 여러 번 커밋해도 5P만 지급
- 주 최대 7회 인정
- 주 최대 35P
- 매일 06:00 KST에 GitHub public events의 전날 `PushEvent`를 조회해 자동 지급
- 프론트에서 `POST /crews/{crewId}/activities/github/sync`를 호출하면 특정 크루의 오늘 활동을 즉시 불러올 수 있음
- private repository 활동은 GitHub public events에 잡히지 않으므로 MVP에서는 인정하지 않음

### 리뷰 정책

- 하루 1회만 인정
- 하루에 여러 번 리뷰해도 5P만 지급
- 주 최대 7회 인정
- 주 최대 35P
- 매일 06:00 KST에 GitHub public events의 전날 `PullRequestReviewEvent`, `PullRequestReviewCommentEvent`를 조회해 자동 지급
- 프론트에서 `POST /crews/{crewId}/activities/github/sync`를 호출하면 특정 크루의 오늘 활동을 즉시 불러올 수 있음
- private repository 활동은 GitHub public events에 잡히지 않으므로 MVP에서는 인정하지 않음

### 미션 성공 정책

- 10P 지급
- 사진 인증 또는 LMS+ 최종 단계 완료 여부 기반
- MVP에서는 수동 검수 필요
- 추후 코치와 상의 후 API 기반 자동화 가능

### 회고 / 기술 블로그 정책

- 20P 지급
- 링크 인증 기반
- MVP에서는 수동 검수 필요
- 추후 크롤링 + AI 기반 확인 가능

---

## 7. 상점 정책

### 7.1 랜덤 박스

가격:

- 10P

| 보상 | 확률 |
|---|---:|
| 꽝 | 25% |
| 5P | 25% |
| 10P | 25% |
| 20P | 15% |
| 50P | 9% |
| 코치 이용권 | 1% |

코치 이용권 당첨 시 코치는 랜덤으로 배정됩니다.

---

### 7.2 코치 이용권

| 상품 | 가격 | 제한 |
|---|---:|---|
| 코치 이용권 - 카페 | 100P | 주 1회 |
| 코치 이용권 - 식사 | 300P | 주 1회 |

- 코치 동의하에 여러 명이 함께 사용할 수 있습니다.
- 구매 시 코치가 랜덤으로 배정됩니다.
- 코치와 상의하여 상품은 점점 추가할 예정입니다.

---

## 8. 코치 목록

### BE

- 브리
- 검프
- 네오
- 구구
- 브라운

### SOFT

- 워니
- 류시
- 리사

### FE

- 준
- 시지프

### ANDROID

- 제임스
- 디노

---

## 9. Enum

### Track

```text
BE
FE
ANDROID
SOFT
```

### ActivityType

```text
COMMIT
REVIEW
MISSION
BLOG
```

### ActivityStatus

```text
APPROVED
PENDING
REJECTED
```

### TicketType

```text
CAFE
MEAL
```

### TicketStatus

```text
AVAILABLE
USED
```

### PointHistoryType

```text
EARN
USE
```

---

## 10. API 명세

## 10.1 로그인

### 로그인

```http
POST /auth/login
```

Request

```json
{
  "githubId": "sauter001"
}
```

GitHub ID는 대소문자를 구분하지 않습니다.
서버에 미리 등록된 크루만 로그인할 수 있습니다.

Response

```json
{
  "crewId": 5,
  "githubId": "Sauter001",
  "nickname": "요크"
}
```

등록되지 않은 GitHub ID인 경우:

```json
{
  "message": "등록된 크루 정보가 아닙니다."
}
```

---

### 내 정보 확인

프론트에서 로그인 유지 상태를 확인하기 위한 API입니다.

```http
GET /auth/me
```

MVP에서는 실제 세션/JWT가 없으므로 구현 방식은 팀에서 단순화할 수 있습니다.
예를 들어 요청 헤더나 쿼리 파라미터로 `githubId`를 전달해도 됩니다.

예시:

```http
GET /auth/me?githubId=sauter001
```

Response

```json
{
  "crewId": 5,
  "githubId": "Sauter001",
  "nickname": "요크",
  "generation": 8,
  "track": "BE",
  "point": 0
}
```

---

## 10.2 메인 페이지

### 내 포인트 요약

```http
GET /crews/{crewId}/points/summary
```

Response

```json
{
  "totalPoint": 120,
  "weeklyEarnedPoint": 55,
  "weeklyLimitPoint": 100,
  "activities": {
    "commit": 15,
    "review": 20,
    "mission": 10,
    "blog": 10
  }
}
```

---

### 크루 랭킹

```http
GET /rankings/crews
```

Response

```json
[
  {
    "rank": 1,
    "nickname": "요크",
    "githubId": "york123",
    "point": 250
  },
  {
    "rank": 2,
    "nickname": "이프",
    "githubId": "if123",
    "point": 120
  }
]
```

---

### 가장 많이 납치된 코치

```http
GET /rankings/coaches
```

이번주에 `USED` 처리된 이용권 수를 기준으로 집계합니다.

Response

```json
[
  {
    "rank": 1,
    "coachName": "브리",
    "usedCount": 7
  },
  {
    "rank": 2,
    "coachName": "네오",
    "usedCount": 5
  }
]
```

---

## 10.3 주간 포인트 체크리스트

### 내 주간 활동 현황

```http
GET /crews/{crewId}/weekly-activities
```

Response

```json
{
  "weekStartDate": "2026-05-18",
  "weekEndDate": "2026-05-24",
  "weeklyEarnedPoint": 55,
  "weeklyLimitPoint": 100,
  "items": [
    {
      "type": "COMMIT",
      "name": "커밋",
      "point": 5,
      "earnedCount": 3,
      "maxCount": 7,
      "earnedPoint": 15,
      "maxPoint": 35
    },
    {
      "type": "REVIEW",
      "name": "리뷰",
      "point": 5,
      "earnedCount": 4,
      "maxCount": 7,
      "earnedPoint": 20,
      "maxPoint": 35
    },
    {
      "type": "MISSION",
      "name": "미션 성공",
      "point": 10,
      "earnedCount": 1,
      "maxCount": 1,
      "earnedPoint": 10,
      "maxPoint": 10
    },
    {
      "type": "BLOG",
      "name": "회고/기술 블로그",
      "point": 20,
      "earnedCount": 0,
      "maxCount": 1,
      "earnedPoint": 0,
      "maxPoint": 20
    }
  ]
}
```

---

## 10.4 활동 인증 / 포인트 적립

커밋과 리뷰는 기본적으로 GitHub public events 기반 자동 동기화로 지급됩니다.
프론트에서는 `POST /crews/{crewId}/activities/github/sync`를 "GitHub 활동 불러오기" 버튼에 연결하면 됩니다.
아래 커밋/리뷰 수동 인증 API는 해커톤 시연, 관리자 보정, 프론트 수동 테스트용으로 사용할 수 있습니다.

### 커밋 인증

```http
POST /crews/{crewId}/activities/commit
```

Request

```json
{
  "activityDate": "2026-05-22",
  "githubUrl": "https://github.com/example/repo/commit/abc"
}
```

Response

```json
{
  "activityType": "COMMIT",
  "earnedPoint": 5,
  "message": "커밋 인증 완료"
}
```

이미 오늘 커밋 인증을 완료한 경우:

```json
{
  "activityType": "COMMIT",
  "earnedPoint": 0,
  "message": "오늘은 이미 커밋 포인트를 획득했습니다"
}
```

---

### 리뷰 인증

```http
POST /crews/{crewId}/activities/review
```

Request

```json
{
  "activityDate": "2026-05-22",
  "reviewUrl": "https://github.com/example/repo/pull/1#discussion_r123"
}
```

Response

```json
{
  "activityType": "REVIEW",
  "earnedPoint": 5,
  "message": "리뷰 인증 완료"
}
```

이미 오늘 리뷰 인증을 완료한 경우:

```json
{
  "activityType": "REVIEW",
  "earnedPoint": 0,
  "message": "오늘은 이미 리뷰 포인트를 획득했습니다"
}
```

---

### 내 GitHub 활동 불러오기

프론트에서 사용자가 로그인 직후 또는 체크리스트 화면에서 누를 수 있는 API입니다.
`activityDate`를 생략하면 오늘 날짜를 기준으로 해당 크루의 GitHub public events를 조회합니다.

```http
POST /crews/{crewId}/activities/github/sync?activityDate=2026-05-22
```

Response

```json
{
  "activityDate": "2026-05-22",
  "crewCount": 1,
  "commitSyncedCount": 1,
  "reviewSyncedCount": 0,
  "message": "내 GitHub 활동 동기화 완료"
}
```

프론트 처리 팁:

- 응답 후 `GET /crews/{crewId}/weekly-activities`와 `GET /crews/{crewId}/points/summary`를 다시 조회하면 화면이 갱신됩니다.
- 이미 지급된 날짜는 중복 지급되지 않으므로 버튼을 여러 번 눌러도 포인트가 중복 증가하지 않습니다.
- GitHub public events 반영 지연이 있을 수 있어 방금 한 활동은 바로 안 잡힐 수 있습니다.

---

### 미션 인증 요청

```http
POST /crews/{crewId}/activities/mission
Content-Type: multipart/form-data
```

미션 인증은 사진 파일을 직접 업로드합니다. (`multipart/form-data`)

Request (form-data)

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `activityDate` | text | O | 활동 일자 (yyyy-MM-dd) |
| `image` | file | O | 미션 인증 사진 파일 |
| `memo` | text | X | 인증 메모 |

업로드한 사진은 서버에 저장되며, 검수 목록에서는 저장된 경로가 `evidenceUrl`로 제공됩니다.

Response

```json
{
  "activityId": 10,
  "status": "PENDING",
  "message": "미션 인증이 검수 대기 상태로 등록되었습니다"
}
```

---

### 블로그 인증 요청

```http
POST /crews/{crewId}/activities/blog
```

Request

```json
{
  "activityDate": "2026-05-22",
  "blogUrl": "https://velog.io/@if/post",
  "memo": "이번 미션 회고입니다."
}
```

Response

```json
{
  "activityId": 11,
  "status": "PENDING",
  "message": "블로그 인증이 검수 대기 상태로 등록되었습니다"
}
```

---

## 10.5 검수 API

### GitHub 활동 수동 동기화

커밋/리뷰 자동 지급은 매일 06:00 KST에 실행됩니다.
시연이나 수동 보정이 필요하면 아래 API로 특정 날짜의 GitHub public events를 즉시 동기화할 수 있습니다.
`activityDate`를 생략하면 전날을 기준으로 동기화합니다.

```http
POST /admin/activities/github/sync?activityDate=2026-05-22
```

Response

```json
{
  "activityDate": "2026-05-22",
  "crewCount": 3,
  "commitSyncedCount": 2,
  "reviewSyncedCount": 1,
  "message": "전체 크루 GitHub 활동 동기화 완료"
}
```

동기화 기준:

- `githubId`로 `GET https://api.github.com/users/{githubId}/events/public` 조회
- 해당 날짜의 `PushEvent`가 있으면 커밋 인증 처리
- 해당 날짜의 `PullRequestReviewEvent` 또는 `PullRequestReviewCommentEvent`가 있으면 리뷰 인증 처리
- 이미 같은 날짜에 승인된 커밋/리뷰가 있으면 중복 지급하지 않음
- 주간 활동별 한도와 전체 주간 100P 한도를 그대로 적용
- GitHub public events는 실시간 보장 API가 아니므로 늦은 밤 활동 누락을 줄이기 위해 자동 동기화는 06:00에 실행

### 검수 대기 목록 조회

```http
GET /admin/activities/pending
```

Response

```json
[
  {
    "activityId": 10,
    "crewId": 1,
    "nickname": "이프",
    "type": "MISSION",
    "evidenceUrl": "https://image-url.com/mission.png",
    "memo": "LMS 최종 단계 완료했습니다.",
    "status": "PENDING"
  }
]
```

---

### 인증 승인

```http
PATCH /admin/activities/{activityId}/approve
```

Response

```json
{
  "activityId": 10,
  "status": "APPROVED",
  "earnedPoint": 10
}
```

---

### 인증 거절

```http
PATCH /admin/activities/{activityId}/reject
```

Request

```json
{
  "reason": "인증 사진에서 완료 여부를 확인할 수 없습니다."
}
```

Response

```json
{
  "activityId": 10,
  "status": "REJECTED",
  "reason": "인증 사진에서 완료 여부를 확인할 수 없습니다."
}
```

---

## 10.6 상점

### 상점 목록 조회

```http
GET /shop/items
```

Response

```json
[
  {
    "itemId": 1,
    "name": "랜덤 박스",
    "type": "RANDOM_BOX",
    "price": 10,
    "description": "확률에 따라 포인트 또는 코치 이용권을 획득합니다."
  },
  {
    "itemId": 2,
    "name": "코치 이용권 - 카페",
    "type": "COACH_CAFE_TICKET",
    "price": 100,
    "weeklyLimit": 1
  },
  {
    "itemId": 3,
    "name": "코치 이용권 - 식사",
    "type": "COACH_MEAL_TICKET",
    "price": 300,
    "weeklyLimit": 1
  }
]
```

---

### 랜덤 박스 구매

```http
POST /crews/{crewId}/shop/random-box
```

Response - 포인트 당첨

```json
{
  "result": "POINT",
  "rewardPoint": 20,
  "message": "20포인트를 획득했습니다!",
  "currentPoint": 130
}
```

Response - 코치 이용권 당첨

```json
{
  "result": "COACH_TICKET",
  "coach": {
    "name": "브리",
    "track": "BE"
  },
  "message": "브리 코치 이용권을 획득했습니다!",
  "currentPoint": 110
}
```

Response - 꽝

```json
{
  "result": "LOSE",
  "rewardPoint": 0,
  "message": "아쉽지만 꽝입니다.",
  "currentPoint": 110
}
```

---

### 코치 이용권 구매

```http
POST /crews/{crewId}/shop/coach-tickets
```

Request

```json
{
  "ticketType": "CAFE"
}
```

Response

```json
{
  "ticketId": 1,
  "ticketType": "CAFE",
  "coach": {
    "name": "네오",
    "track": "BE"
  },
  "price": 100,
  "message": "네오 코치 카페 이용권을 획득했습니다.",
  "currentPoint": 20
}
```

---

## 10.7 코치

### 코치 목록 조회

```http
GET /coaches
```

Response

```json
[
  {
    "id": 1,
    "name": "브리",
    "track": "BE"
  },
  {
    "id": 2,
    "name": "검프",
    "track": "BE"
  },
  {
    "id": 3,
    "name": "워니",
    "track": "SOFT"
  }
]
```

---

## 10.8 이용권

### 내 이용권 목록 조회

```http
GET /crews/{crewId}/tickets
```

Response

```json
[
  {
    "ticketId": 1,
    "type": "CAFE",
    "coachName": "네오",
    "status": "AVAILABLE",
    "createdAt": "2026-05-22T13:00:00"
  }
]
```

---

### 이용권 사용 처리

```http
PATCH /crews/{crewId}/tickets/{ticketId}/use
```

Response

```json
{
  "ticketId": 1,
  "status": "USED",
  "message": "이용권 사용 완료"
}
```

---

## 10.9 포인트 내역

### 내 포인트 내역 조회

```http
GET /crews/{crewId}/point-histories
```

Response

```json
[
  {
    "type": "EARN",
    "amount": 5,
    "reason": "커밋 인증",
    "createdAt": "2026-05-22T13:00:00"
  },
  {
    "type": "USE",
    "amount": -10,
    "reason": "랜덤 박스 구매",
    "createdAt": "2026-05-22T13:10:00"
  }
]
```

---

## 11. 추천 ERD

### Crew

| 컬럼 | 설명 |
|---|---|
| id | 크루 ID |
| githubId | GitHub ID |
| nickname | 닉네임 |
| generation | 기수 |
| track | 분야 |
| point | 현재 보유 포인트 |

---

### Activity

| 컬럼 | 설명 |
|---|---|
| id | 활동 ID |
| crewId | 크루 ID |
| type | COMMIT, REVIEW, MISSION, BLOG |
| point | 지급 포인트 |
| status | APPROVED, PENDING, REJECTED |
| evidenceUrl | 인증 URL |
| memo | 인증 메모 |
| activityDate | 활동 일자 |
| createdAt | 생성 시각 |

---

### Coach

| 컬럼 | 설명 |
|---|---|
| id | 코치 ID |
| name | 코치명 |
| track | 분야 |

---

### Ticket

| 컬럼 | 설명 |
|---|---|
| id | 이용권 ID |
| crewId | 크루 ID |
| coachId | 코치 ID |
| type | CAFE, MEAL |
| status | AVAILABLE, USED |
| createdAt | 생성 시각 |
| usedAt | 사용 시각 |

---

### PointHistory

| 컬럼 | 설명 |
|---|---|
| id | 포인트 내역 ID |
| crewId | 크루 ID |
| type | EARN, USE |
| amount | 포인트 변화량 |
| reason | 사유 |
| createdAt | 생성 시각 |

---

## 12. MVP 구현 우선순위

1. `POST /auth/login`
2. `GET /auth/me`
3. `GET /crews/{crewId}/points/summary`
4. `POST /crews/{crewId}/activities/github/sync`
5. `POST /crews/{crewId}/activities/mission`
6. `POST /crews/{crewId}/activities/blog`
7. `GET /shop/items`
8. `POST /crews/{crewId}/shop/random-box`
9. `POST /crews/{crewId}/shop/coach-tickets`
10. `GET /rankings/crews`
11. `GET /rankings/coaches`
12. `GET /crews/{crewId}/weekly-activities`
13. `GET /crews/{crewId}/tickets`
14. `PATCH /crews/{crewId}/tickets/{ticketId}/use`

---

## 13. 구현 메모

### 포인트 처리

- `Crew.point`는 현재 포인트 캐시값으로 사용합니다.
- 모든 포인트 증감은 `PointHistory`에 기록합니다.
- 포인트 차감과 지급은 하나의 트랜잭션으로 처리합니다.
- `Crew.version`을 사용해 포인트 사용 동시성 충돌을 방어합니다.

### 주간 제한

- 커밋과 리뷰는 하루 1회만 인정합니다.
- 커밋과 리뷰는 각각 주 최대 7회 인정합니다.
- 전체 주간 획득 포인트는 최대 100P입니다.

### 검수

- 커밋 / 리뷰는 GitHub public events 자동 동기화로 즉시 승인 처리됩니다.
- 커밋 / 리뷰 수동 인증 API는 테스트와 보정 용도로 유지합니다.
- 미션 / 블로그는 `PENDING` 상태로 생성합니다.
- 관리자가 승인하면 포인트를 지급합니다.

### 상점

- 랜덤박스 구매 시 먼저 10P를 차감합니다.
- 당첨 결과에 따라 포인트 또는 이용권을 지급합니다.
- 코치 이용권 구매 시 먼저 포인트를 차감하고 랜덤 코치를 배정합니다.
- 코치 이용권은 주 1회 구매 제한을 둡니다.

---

## 14. 향후 확장 아이디어

- GitHub OAuth 연동
- GitHub 인증 토큰 기반 API rate limit 완화
- LMS API 기반 미션 완료 자동 검증
- 블로그 크롤링 기반 작성 여부 확인
- AI 기반 회고 / 기술 블로그 검수
- 코치별 이용권 수량 제한
- 코치 승인 플로우 추가
- 크루 CSV 기반 사전 인증
