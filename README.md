1. 획득 포인트 최대 지정 (주 100점)

* 커밋 1일 1회(여러번 커밋해도 한 번으로 간주) - 5점, 주 7회 - 35점
* 리뷰 1일 1회(여러번 리뷰써도 한 번으로 간주) - 5점, 주 7회 - 35점
* 미션 성공 (사진 인증, LMS+ : 최종 단계 완료 했는지, 추후 코치와 상의 후 api 기반 자동화 연동 가능) → 검수 필요 - 10점
* 회고 및 기술 블로그 작성 및 인증 (링크, 나중에 크롤링 + AI 기반 확인 가능) → 검수 필요 - 20점
2. 포인트 상점 목록
* 랜덤 박스 (10p)
* 꽝(25%), 5p(25%), 10p(25%), 20p(15%), 50p(9%), 코치 이용권(1%, 코치는 랜덤)
* 코치 이용권 (주 1회, 코치 동의하 여러명 가능)
* 커피 100p
* 식사 300p
* 코치 목록...
* 코치 상의하에 점점 더 추가하겠다. (기대해도 좋다.)
3. 크루 인증? → oauth 우선 뺌
   → 닉네임 필요, 기수 필요, 분야, Github 아이디 (로그인 보안 일단 무시)
   → 자체 csv로 우선 인증 (후순위)

역할:
2번 3번 - 요크
1번 - 이프
UI - 맥스

UI

* 크루 회원가입 페이지
    * 입력: 깃헙 아이디, 닉네임
    * 기수: 우선 8기 고정 (입력 ㄴ, 하지만 서버에서는 저장)
    * 분야: 백엔드, 프론트, 안드로이드 (선택 박스 리스트업)
* 로그인 페이지
    * 깃헙 아이디로만 로그인
    * 보안은 신경쓸게 아님, 해커톤이기 때문에
* 포인트 상점
    * 랜덤 박스
    * 코치 이용권 - 카페 (주 1회)
    * 코치 이용권 - 식사 (주 1회)
    * 코치 이용권은 코치들이 항상 랜덤으로 나온다.
        * BE: 브리 , 검프, 네오, 구구 , 브라운,
        * SOFT: 워니, 류시, 리사
        * FE : 준, 시지프,
        * AN: 제임스, 디노
* 주간 포인트 체크 리스트
    * 알아서
* 메인 페이지
    * 보유 포인트
        * 이번주 획득 포인트, 주간 누적 포인트
        * 커밋, 리뷰, 미션, 블로그에 따른 포인트 현황
    * 상점, 주간 포인트 페이지 등
    * 이번주 가장 많이 납치된 코치
    * 크루 별 랭킹



API 초안

1. 크루 회원가입 / 로그인

크루 회원가입


POST /crews

{
"githubId": "if123",
"nickname": "이프",
"track": "BE"
}

응답


{
"id": 1,
"githubId": "if123",
"nickname": "이프",
"generation": 8,
"track": "BE",
"point": 0
}


로그인


POST /auth/login

{
"githubId": "if123"
}

응답


{
"crewId": 1,
"githubId": "if123",
"nickname": "이프"
}


2. 메인 페이지

내 포인트 요약


GET /crews/{crewId}/points/summary

응답


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


크루 랭킹


GET /rankings/crews

응답


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


가장 많이 납치된 코치


GET /rankings/coaches

응답


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


3. 주간 포인트 체크리스트

내 주간 활동 현황


GET /crews/{crewId}/weekly-activities

응답


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


4. 활동 인증 / 포인트 적립

커밋 인증


POST /crews/{crewId}/activities/commit

{
"activityDate": "2026-05-22",
"githubUrl": "https://github.com/example/repo/commit/abc"
}

응답


{
"activityType": "COMMIT",
"earnedPoint": 5,
"message": "커밋 인증 완료"
}

이미 오늘 커밋 인증했다면:


{
"activityType": "COMMIT",
"earnedPoint": 0,
"message": "오늘은 이미 커밋 포인트를 획득했습니다"
}


리뷰 인증


POST /crews/{crewId}/activities/review

{
"activityDate": "2026-05-22",
"reviewUrl": "https://github.com/example/repo/pull/1#discussion_r123"
}


미션 인증 요청


POST /crews/{crewId}/activities/mission

{
"activityDate": "2026-05-22",
"imageUrl": "https://image-url.com/mission.png",
"memo": "LMS 최종 단계 완료했습니다."
}

응답


{
"activityId": 10,
"status": "PENDING",
"message": "미션 인증이 검수 대기 상태로 등록되었습니다"
}


블로그 인증 요청


POST /crews/{crewId}/activities/blog

{
"activityDate": "2026-05-22",
"blogUrl": "https://velog.io/@if/post",
"memo": "이번 미션 회고입니다."
}


5. 검수 API

검수 대기 목록


GET /admin/activities/pending

응답


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


인증 승인


PATCH /admin/activities/{activityId}/approve

응답


{
"activityId": 10,
"status": "APPROVED",
"earnedPoint": 10
}


인증 거절


PATCH /admin/activities/{activityId}/reject

{
"reason": "인증 사진에서 완료 여부를 확인할 수 없습니다."
}


6. 상점

상점 목록 조회


GET /shop/items

응답


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


랜덤 박스 구매


POST /crews/{crewId}/shop/random-box

응답 예시


{
"result": "POINT",
"rewardPoint": 20,
"message": "20포인트를 획득했습니다!",
"currentPoint": 130
}

코치 이용권 당첨 시:


{
"result": "COACH_TICKET",
"coach": {
"name": "브리",
"track": "BE"
},
"message": "브리 코치 이용권을 획득했습니다!",
"currentPoint": 110
}


코치 이용권 구매


POST /crews/{crewId}/shop/coach-tickets

{
"ticketType": "CAFE"
}

응답


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


7. 코치 목록

코치 목록 조회


GET /coaches

응답


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


8. 내 이용권 목록


GET /crews/{crewId}/tickets

응답


[
{
"ticketId": 1,
"type": "CAFE",
"coachName": "네오",
"status": "AVAILABLE",
"createdAt": "2026-05-22T13:00:00"
}
]


이용권 사용 처리


PATCH /crews/{crewId}/tickets/{ticketId}/use

응답


{
"ticketId": 1,
"status": "USED",
"message": "이용권 사용 완료"
}


최소 구현 우선순위

1. POST /crews
2. POST /auth/login
3. GET /crews/{crewId}/points/summary
4. POST /crews/{crewId}/activities/commit
5. POST /crews/{crewId}/activities/review
6. POST /crews/{crewId}/activities/mission
7. POST /crews/{crewId}/activities/blog
8. GET /shop/items
9. POST /crews/{crewId}/shop/random-box
10. POST /crews/{crewId}/shop/coach-tickets
11. GET /rankings/crews
