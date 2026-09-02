-- 게임방 비밀번호 제거 마이그레이션 (PostgreSQL)
--
-- game_rooms.password_hash 가 NOT NULL 이라, 엔티티에서 필드를 뺀 새 jar 이 뜨면
-- INSERT 에 이 컬럼이 빠져 방 생성이 전부 실패한다. 배포 전에 실행할 것.
--
-- 실행:
--   PGPASSWORD=<pw> psql -h localhost -U triply_app -d triply -f migration-drop-room-password.sql

\set ON_ERROR_STOP on

\echo ''
\echo '### 적용 전 - password_hash 컬럼이 있어야 정상'
select column_name, is_nullable
from information_schema.columns
where table_name = 'game_rooms' and column_name = 'password_hash';

BEGIN;
ALTER TABLE game_rooms DROP COLUMN IF EXISTS password_hash;
COMMIT;

\echo ''
\echo '### 적용 후 - 0 행이어야 정상'
select column_name
from information_schema.columns
where table_name = 'game_rooms' and column_name = 'password_hash';

\echo ''
\echo '### 방 개수 (변하지 않아야 정상)'
select count(*) as rooms from game_rooms;
