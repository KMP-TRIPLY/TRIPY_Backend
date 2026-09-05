-- 방장 위임 타이머 기준 시각.
-- 방이 WAITING 이고 활성 멤버 수가 정원에 도달하면 ready_since_at 이 찍힌다.
-- 이 값이 GAME_ROOM_HOST_DELEGATION_TIMEOUT_MINUTES 만큼 지나도 시작되지 않으면 방장이 입장 순서대로 위임된다.
ALTER TABLE game_rooms ADD COLUMN IF NOT EXISTS ready_since_at timestamp;
