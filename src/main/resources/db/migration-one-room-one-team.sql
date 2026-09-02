-- 한 방 = 한 팀 전환 마이그레이션 (PostgreSQL)
-- ddl-auto: update 는 컬럼을 지우거나 이름을 바꾸지 못한다. 배포 전에 직접 실행할 것.
-- 실행 전 dump 를 떠두는 편이 안전하다.

BEGIN;

-- 1) 팀장 개념 제거. 역할은 방장(game_rooms.host_user_id) 하나만 남는다.
ALTER TABLE teams DROP COLUMN IF EXISTS leader_user_id;
ALTER TABLE team_members DROP COLUMN IF EXISTS role;

-- 2) 정원의 의미가 '팀 수'에서 '방 멤버 수'로 바뀐다.
ALTER TABLE game_rooms RENAME COLUMN max_teams TO max_members;

-- 3) 기존에 한 방에 팀이 둘 이상 있던 데이터 확인용.
--    이제 방마다 팀 하나만 쓰이므로(가장 작은 id) 나머지 팀의 멤버는 조회에서 빠진다.
--    아래 쿼리로 대상이 있는지 먼저 확인하고, 있으면 어떻게 합칠지 결정할 것.
--   SELECT game_room_id, count(*) FROM teams GROUP BY game_room_id HAVING count(*) > 1;

COMMIT;

-- 4) 방 안 순위 제거에 따른 랭킹 유형 이름 변경 (TEAM -> ROOM).
--    rankings.ranking_type 은 enum 문자열로 저장되므로 값도 같이 바꿔야 한다.
BEGIN;
UPDATE rankings SET ranking_type = 'ROOM' WHERE ranking_type = 'TEAM';
COMMIT;
