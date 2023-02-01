-- apply changes
alter table url alter column created_at type timestamptz using created_at::timestamptz;
