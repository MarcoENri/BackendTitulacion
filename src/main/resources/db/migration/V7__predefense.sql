-- V7__predefensa.sql

create table if not exists predefense_window (
                                                 id bigserial primary key,
                                                 academic_period_id bigint not null references academic_period(id),
    career_id bigint null references career(id),

    starts_at timestamp not null,
    ends_at timestamp not null,

    is_active boolean not null default true,
    created_at timestamp not null default now(),
    closed_at timestamp null
    );

create index if not exists idx_predefense_window_period
    on predefense_window(academic_period_id);

create index if not exists idx_predefense_window_career
    on predefense_window(career_id);

create table if not exists predefense_slot (
                                               id bigserial primary key,
                                               window_id bigint not null references predefense_window(id) on delete cascade,

    starts_at timestamp not null,
    ends_at timestamp not null,

    created_at timestamp not null default now()
    );

create index if not exists idx_predefense_slot_window
    on predefense_slot(window_id);

create table if not exists predefense_booking (
                                                  id bigserial primary key,
                                                  slot_id bigint not null unique references predefense_slot(id) on delete cascade,
    student_id bigint not null references student(id),

    created_at timestamp not null default now()
    );

create index if not exists idx_predefense_booking_student
    on predefense_booking(student_id);

create table if not exists predefense_booking_jury (
                                                       booking_id bigint not null references predefense_booking(id) on delete cascade,
    jury_user_id bigint not null references app_user(id),

    primary key (booking_id, jury_user_id)
    );

create index if not exists idx_predefense_booking_jury_jury
    on predefense_booking_jury(jury_user_id);

create table if not exists predefense_observation (
                                                      id bigserial primary key,
                                                      booking_id bigint not null references predefense_booking(id) on delete cascade,

    author_user_id bigint null references app_user(id),
    author_name varchar(255) not null,

    text text not null,

    created_at timestamp not null default now()
    );

create index if not exists idx_predefense_observation_booking
    on predefense_observation(booking_id);
