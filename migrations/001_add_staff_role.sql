-- Add STAFF role to existing Quickvnt databases.
alter table public.profiles drop constraint if exists profiles_role_check;
alter table public.profiles
  add constraint profiles_role_check
  check (role in ('ATTENDEE', 'ORGANIZER', 'STAFF'));
