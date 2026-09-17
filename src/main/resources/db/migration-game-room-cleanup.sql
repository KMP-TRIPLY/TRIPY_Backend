-- 방치된 방 정리 스케줄러는 스키마를 바꾸지 않고 status/created_at/started_at 만 읽는다.
-- 조회 조건에 맞춘 인덱스만 보강한다.

create index if not exists idx_game_rooms_status_created_at on game_rooms (status, created_at);
create index if not exists idx_game_rooms_status_started_at on game_rooms (status, started_at);
