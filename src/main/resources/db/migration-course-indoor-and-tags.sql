-- 코스 실내 여부·태그 + 스팟 실내 여부.
-- ddl-auto=update 로도 생성되지만, 관리형 DB 에 수동으로 반영할 때 쓰는 기준 스크립트다.

alter table courses
    add column if not exists indoor_type varchar(10) not null default 'MIXED';

alter table course_spots
    add column if not exists is_indoor boolean not null default false;

create table if not exists course_tags (
    course_id bigint      not null references courses (id) on delete cascade,
    tag       varchar(20) not null,
    constraint uk_course_tags_course_tag unique (course_id, tag)
);

create index if not exists idx_course_tags_tag on course_tags (tag);
