# 게임방 구조와 흐름

한 방 = 한 팀 전환 이후의 게임방 도메인 정리.

---

## 1. 구성 요소

```
GameRoom (게임방)           ← 사용자가 인식하는 단위
  host_user_id = 방장
  room_code (6자리) + password_hash
  course_id (어느 코스를 도는지)
  max_members (정원)
  status: WAITING → RUNNING → FINISHED
  │
  ├─ Team (내부용, 방당 정확히 1행)
  │    team_name  ← 방 이름
  │    total_score, hint_count_used, status, rank
  │    └─ GameProgress (스팟별 진행) → MissionAttempt (제출 이력)
  │
  └─ TeamMember (방 멤버들)
       user_id, joined_at, left_at, is_active
```

`Team` 은 API에 노출되지 않는다. 점수와 진행상황이 붙어 있는 내부 테이블이며 방과 1:1이다.
클라이언트는 방(`roomId`)과 멤버만 다룬다.

---

## 2. 역할

역할은 **방장**(`GameRoom.host`) 하나다. 팀장(`Team.leader`)과 멤버 역할(`TeamMember.role`)은 제거했다.

방장만 가능한 동작 — 모두 `validateHost()` 통과 필요 (`GameRoomServiceImpl.java:308`):

| 동작 | 메서드 |
|---|---|
| 게임 시작 | `startRoom` |
| 코스 변경 | `changeCourse` |
| 게임 종료 | `endRoom` |
| 멤버 강퇴 | `kickMember` (자기 자신은 불가) |

그리고 방장은 **방을 나갈 수 없다** — `leaveRoom` 이 `GAME_ROOM_ACCESS_DENIED`(403)로 막는다.
방을 시작·종료할 사람이 사라지면 남은 멤버가 방에 갇히기 때문이다.

---

## 3. 생애주기

### ① 방 생성 — `POST /api/game-rooms`

```json
{ "courseId": 1, "password": "1234", "roomName": "공주 원정대", "maxMembers": 6 }
```

1. 코스 존재 확인
2. 방 코드 생성 (중복이면 재추첨, `generateRoomCode`)
3. 비밀번호 해시 저장
4. 생성자를 방장으로 지정, 동시에 `Team` 1행 + `TeamMember` 1행 생성 (`createTeamWithMember`)
5. `ROOM_CREATED` 이벤트 발행

### ② 참여 — `POST /api/game-rooms/join`

```json
{ "roomCode": "AB3D5F", "password": "1234" }
```

`joinRoom` (`GameRoomServiceImpl.java:88`) 의 분기:

1. 비밀번호 불일치 → `INVALID_GAME_ROOM_PASSWORD`
2. **이미 이 방의 멤버** → `rejoinRoom()` 으로 재입장 처리 (4장 참고)
3. 신규 참여
   - `WAITING` 상태인지 확인
   - 정원 확인: `countByTeamGameRoomId >= maxMembers` 면 `ROOM_CAPACITY_EXCEEDED`
   - 방의 유일한 팀에 합류
   - `MEMBER_JOINED` 이벤트 발행

팀 선택 개념이 없다. 이전의 `teamId` / `teamName` 요청 필드는 삭제했다.

### ③ 시작 — `POST /api/game-rooms/start`

방장 전용. `WAITING` → `RUNNING`, 팀 상태도 `PLAYING` 으로 전환. `ROOM_STARTED` 발행.

### ④ 플레이

모든 경로가 `roomId` 기준이며, 서버가 `teamOfRoom(roomId)` 로 내부 팀을 찾는다.

| 동작 | 엔드포인트 |
|---|---|
| 스팟 도착 인증 | `POST /api/game-rooms/{roomId}/spots/{spotId}/arrive` |
| 스팟 미션 조회 | `GET /api/game-rooms/{roomId}/spots/{spotId}/missions` |
| 힌트 요청 | `POST /api/missions/{missionId}/hint` `{roomId}` |
| 미션 제출 (퀴즈) | `POST /api/missions/{missionId}/submit` `{roomId, submittedValue}` |
| 미션 제출 (사진) | `POST /api/missions/{missionId}/photo?roomId=` (multipart) |
| 진행 현황 | `GET /api/game-rooms/{roomId}/progress` |

- 도착 인증은 GPS 검증 + 스팟 순서 검사(`validateSequentialOrder`) 후 해당 스팟의 미션을 활성화한다.
- 미션 조회 응답에는 정답이 포함되지 않는다.
- 힌트는 최초 열람 시 감점이 예약되고, **요청자 본인**으로 이력이 기록된다.
- 사진 계열(PHOTO / AR / VOICE)은 문자열 제출로 통과하지 못하도록 이미지 업로드 전용 경로만 허용한다.

### ⑤ 종료 — `POST /api/game-rooms/{id}/end`

방장 전용. `endRoom` (`GameRoomServiceImpl.java:206`):

1. 팀 점수 확정 (`team.finish`)
2. `gameRoom.finish()`
3. `saveFinalRankings()` 가 `rankings` 테이블에 두 종류를 저장
   - `ROOM` 타입 1행 — 이 방의 최종 점수
   - `PERSONAL` 타입 N행 — 멤버별 점수
4. `ROOM_FINISHED` 발행

여기 쌓인 기록이 코스 랭킹의 재료가 된다.

---

## 4. 하차와 재입장

### 나가기 — `POST /api/game-rooms/{roomId}/leave`

`leaveRoom` (`GameRoomServiceImpl.java:153`):

| 조건 | 동작 |
|---|---|
| 방장 | 즉시 403. 나갈 방법이 없다 |
| `WAITING` | `leaveWaitingRoom` — `TeamMember` 행 **삭제**, 이력 없음 → 다시 들어올 수 있다 |
| `RUNNING` | `leaveRunningRoom` — `is_active=false` + `TeamLeaveHistory` 에 사유와 `preservedScore` 기록 → **재입장 불가** |

### 재입장

별도 API가 없다. 참여 API(`/game-rooms/join`)를 다시 호출하면 `rejoinRoom` (`:265`)이 처리한다.

1. `FINISHED` 방이면 거부
2. **하차 이력이 있으면 거부** (`LEFT_ROOM_CANNOT_REJOIN`)
3. `is_active=false` 였으면 `rejoin()` 으로 되살림 — 팀은 원래대로
4. `MEMBER_REJOINED` 발행

> **스스로 나간 것과 튕긴 것의 구분 기준이 하차 이력이다.**
> 앱이 죽거나 네트워크가 끊긴 경우 클라이언트가 아무 API도 부르지 않으므로 이력이 남지 않는다.
> 따라서 튕긴 사람은 복귀할 수 있고, 하차 버튼을 누른 사람은 못 돌아온다.

### 방장이 앱을 끄고 다시 들어오는 경우

하차 자체가 막혀 있어 하차 이력이 생길 수 없으므로 항상 재입장이 통과한다.
`host_user_id` 도 그대로이므로 방장 권한(시작·종료·강퇴)이 그대로 복귀한다.

---

## 5. 순위

| 범위 | 내용 | API |
|---|---|---|
| 방 안 | 멤버끼리 개인 점수 순위 (실시간) | `GET /api/rankings/live?gameRoomId=` |
| 방 밖 | 같은 코스를 돈 **방끼리** 최종 순위 | `GET /api/rankings/courses/{courseId}?mode=ROOM` |
| 방 밖 | 같은 코스의 **개인** 최종 순위 | `GET /api/rankings/courses/{courseId}?mode=PERSONAL` |

- `live` 는 진행 중(`RUNNING`)인 방에서만 조회되며, 그 방의 미션 제출 기록을 집계한다.
- 코스 랭킹은 `endRoom` 이 쌓아둔 `rankings` 행을 점수순으로 읽는다. 코스가 곧 "게임 한 종류"이므로 순위가 게임별로 나뉜다.
- 방 안 팀 순위(`GET /game-rooms/{roomId}/rankings`, live의 `mode=TEAM`)는 항상 1행이라 삭제했다.

---

## 6. 동시 진행 격리

여러 방이 같은 코스로 동시에 게임해도 서로 영향이 없다.

| 자원 | 격리 방식 |
|---|---|
| 진행상황 | `game_progress` 를 `(team_id, spot_id)` 로 조회. 팀은 방에 종속 |
| 점수 | `MissionAttempt` → `GameProgress` → `Team` 체인 |
| 실시간 이벤트 | `/topic/game-rooms/{roomId}` 방별 채널 |
| 최종 랭킹 | `deleteByGameRoomId` 후 해당 방 것만 삽입 |
| 사진 저장 키 | 날짜 / 미션 / 팀 / UUID 조합 |

코스 랭킹에 여러 방의 기록이 함께 쌓이는 것은 의도된 동작이다 (방 vs 방 경쟁).

### 알려진 공유 자원 이슈

- **쿠폰 재고** (`RewardServiceImpl.java:183`) — `countByCouponId >= maxIssueCount` 확인과 발급 사이에 락이 없다. 두 방이 동시에 정산하면 한도를 넘겨 발급될 수 있다.
- **방 코드** (`generateRoomCode`) — 동시에 같은 코드를 뽑으면 유니크 제약에 걸려 500이 난다. 확률은 낮고(32자 6자리) 재시도로 감쌀 수 있다.

---

## 7. 배포 전 필수 작업

`src/main/resources/db/migration-one-room-one-team.sql` 을 직접 실행해야 한다.
`ddl-auto: update` 는 컬럼을 삭제하거나 이름을 바꾸지 못한다.

```sql
ALTER TABLE teams DROP COLUMN IF EXISTS leader_user_id;
ALTER TABLE team_members DROP COLUMN IF EXISTS role;
ALTER TABLE game_rooms RENAME COLUMN max_teams TO max_members;

-- Hibernate 가 enum 컬럼에 만들어둔 CHECK 제약이 'ROOM' 을 거부하므로
-- 제약을 먼저 갈아야 한다
ALTER TABLE rankings DROP CONSTRAINT IF EXISTS rankings_ranking_type_check;
UPDATE rankings SET ranking_type = 'ROOM' WHERE ranking_type = 'TEAM';
ALTER TABLE rankings ADD CONSTRAINT rankings_ranking_type_check
  CHECK (ranking_type IN ('ROOM', 'PERSONAL'));
```

컬럼을 지우지 않으면 `NOT NULL` 위반으로 방 생성이 전부 실패한다.

한 방에 팀이 둘 이상인 기존 데이터가 있으면 첫 팀(가장 작은 id)만 쓰이고
나머지 팀의 멤버는 조회에서 빠진다. 적용 전 아래로 확인할 것:

```sql
SELECT game_room_id, count(*) FROM teams GROUP BY game_room_id HAVING count(*) > 1;
```

### 클라이언트 영향

| 이전 | 이후 |
|---|---|
| `POST /game-rooms` `{teamName, maxTeams}` | `{roomName, maxMembers}` |
| `POST /game-rooms/join` `{teamId, teamName}` | 두 필드 삭제 |
| `GET /game-rooms/{roomId}/teams/{teamId}/progress` | `GET /game-rooms/{roomId}/progress` |
| `GET /teams/{teamId}/members` | `GET /game-rooms/{roomId}/members` |
| `GET .../missions?teamId=` | 파라미터 삭제 |
| `POST /missions/{id}/photo?teamId=` | `?roomId=` |
| `POST /missions/{id}/hint` `{teamId}` | `{roomId}` |
| `POST /missions/{id}/submit` `{teamId}` | `{roomId}` |
| `POST .../arrive` `{teamId, lat, lng}` | `{lat, lng}` |
| `GET /game-rooms/{roomId}/rankings` | 삭제 |
| `GET /rankings/live?mode=` | `mode` 파라미터 삭제 |

응답 필드도 `teamId` / `teamName` → `roomId` / `roomName` 으로 변경.
DTO는 `TeamProgressResponse` → `RoomProgressResponse`, `TeamResponse` → `RoomStateResponse`
(참여 응답의 `team` 블록 → `state`).

---

## 8. 전환 중 함께 고친 버그

1. **힌트 요청자가 항상 팀장으로 기록** — `.user(team.getLeader())` → 실제 요청자 (`GamePlayServiceImpl.java:166`)
2. **도달 불가 fallback** — 제출자 조회의 `orElse(team.getLeader())`. 직전에 팀원 검증을 이미 통과하므로 도달할 수 없는 코드였다. 제거하면서 같은 조회를 두 번 하던 중복 쿼리도 사라졌다
3. **정원 검사 부재** — 이전에는 팀 수만 셌기 때문에 한 방 = 한 팀이 되는 순간 무제한 입장이 됐다

---

## 9. 남은 작업

- `Team` 테이블은 방당 1행으로 내부에만 존재한다 (`teamOfRoom` 의 `ponytail:` 주석). `GameRoom` 으로 흡수하려면 점수·진행상황 컬럼과 reward·ranking 도메인의 FK 이전이 필요하다
- `Team.rank` 와 `rankings.rank`(ROOM 타입)는 방 안 등수라 항상 1이다. 코스 랭킹 조회 시 `index+1` 로 다시 매기므로 실질 영향은 없지만 죽은 값이다
- 쿠폰 재고 동시성 (6장)
- `Leaderboard` / `LeaderboardScope` 는 참조가 0인 죽은 엔티티
- 컴파일 미검증 — 작업 환경에 JDK가 없었다. `./gradlew compileJava compileTestJava` 확인 필요
