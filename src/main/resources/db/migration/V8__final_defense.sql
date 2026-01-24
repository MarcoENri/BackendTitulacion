-- V8__final_defense.sql

create table if not exists final_defense_window (
                                                    id bigserial primary key,
                                                    academic_period_id bigint not null references academic_period(id),
    career_id bigint null references career(id),
    starts_at timestamp not null,
    ends_at timestamp not null,
    is_active boolean not null default true,
    created_at timestamp not null default now()
    );

create table if not exists final_defense_slot (
                                                  id bigserial primary key,
                                                  window_id bigint not null references final_defense_window(id) on delete cascade,
    starts_at timestamp not null,
    ends_at timestamp not null,
    created_at timestamp not null default now()
    );

create table if not exists final_defense_group (
                                                   id bigserial primary key,
                                                   academic_period_id bigint not null references academic_period(id),
    career_id bigint not null references career(id),
    project_name varchar(255) null,
    created_at timestamp not null default now()
    );

create table if not exists final_defense_group_member (
                                                          group_id bigint not null references final_defense_group(id) on delete cascade,
    student_id bigint not null references student(id),
    primary key (group_id, student_id)
    );

create table if not exists final_defense_booking (
                                                     id bigserial primary key,
                                                     slot_id bigint not null unique references final_defense_slot(id),
    group_id bigint not null references final_defense_group(id),
    status varchar(30) not null default 'SCHEDULED',
    final_average numeric(5,2) null,
    verdict varchar(20) null,
    final_observations text null,
    acta_path varchar(500) null,
    created_at timestamp not null default now()
    );

create table if not exists final_defense_booking_jury (
                                                          booking_id bigint not null references final_defense_booking(id) on delete cascade,
    jury_user_id bigint not null references app_user(id),
    primary key (booking_id, jury_user_id)
    );

create table if not exists final_defense_evaluation (
                                                        id bigserial primary key,
                                                        booking_id bigint not null references final_defense_booking(id) on delete cascade,
    jury_user_id bigint not null references app_user(id),
    rubric_score integer not null,
    extra_score integer not null,
    total_score integer not null,
    verdict varchar(20) not null,         -- 👈 recomendado
    observations text null,
    created_at timestamp not null default now(),
    unique (booking_id, jury_user_id)
    );
