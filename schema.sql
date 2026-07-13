-- =========================================================================
-- QUICKVNT DATABASE SCHEMA - MVP SAAS FOR MICE EVENT MANAGEMENT
-- Optimized following Supabase Postgres Best Practices
-- =========================================================================

-- Enable required extensions
create extension if not exists "uuid-ossp";

-- -------------------------------------------------------------------------
-- 1. PROFILES TABLE (1:1 with auth.users)
-- -------------------------------------------------------------------------
create table public.profiles (
  id uuid references auth.users(id) on delete cascade primary key,
  name text not null,
  role text not null check (role in ('ATTENDEE', 'ORGANIZER', 'STAFF')),
  created_at timestamptz default now() not null
);

-- Enable RLS on profiles
alter table public.profiles enable row level security;
alter table public.profiles force row level security;

-- Profiles Security Policies (Optimized with subquery-caching format)
create policy select_profiles on public.profiles
  for select to authenticated using (true);

create policy update_own_profile on public.profiles
  for update to authenticated using (id = (select auth.uid()));


-- -------------------------------------------------------------------------
-- 2. EVENTS TABLE
-- -------------------------------------------------------------------------
create table public.events (
  id uuid default gen_random_uuid() primary key,
  organizer_id uuid references public.profiles(id) on delete cascade not null,
  title text not null,
  description text,
  category text,
  location text not null,
  date_start timestamptz not null,
  date_end timestamptz not null,
  capacity int not null check (capacity > 0),
  banner_url text,
  status text default 'DRAFT' not null check (status in ('DRAFT', 'PUBLISHED', 'CLOSED', 'CANCELLED')),
  custom_form_schema jsonb default '{}'::jsonb not null,
  created_at timestamptz default now() not null,
  constraint check_dates check (date_end >= date_start)
);

-- Enable RLS on events
alter table public.events enable row level security;
alter table public.events force row level security;

-- Foreign key indexes (schema-foreign-key-indexes.md)
create index events_organizer_id_idx on public.events(organizer_id);

-- Partial index for high-performance marketplace filtering (query-partial-indexes.md)
create index events_published_status_date_start_idx 
  on public.events(status, date_start) 
  where status = 'PUBLISHED';

-- Events Security Policies (Optimized cache patterns)
create policy select_published_events on public.events
  for select to authenticated, anon using (status = 'PUBLISHED');

create policy select_own_events on public.events
  for select to authenticated using (organizer_id = (select auth.uid()));

create policy manage_own_events on public.events
  for all to authenticated using (
    organizer_id = (select auth.uid()) 
    and (select role from public.profiles where id = (select auth.uid())) = 'ORGANIZER'
  );


-- -------------------------------------------------------------------------
-- 3. TICKETS TABLE
-- -------------------------------------------------------------------------
create table public.tickets (
  id uuid default gen_random_uuid() primary key,
  event_id uuid references public.events(id) on delete cascade not null,
  user_id uuid references public.profiles(id) on delete cascade not null,
  form_response jsonb default '{}'::jsonb not null,
  qr_signature text not null,
  status text default 'REGISTERED' not null check (status in ('REGISTERED', 'CHECKED_IN', 'CANCELLED')),
  registered_at timestamptz default now() not null,
  constraint unique_user_event_ticket unique(user_id, event_id)
);

-- Enable RLS on tickets
alter table public.tickets enable row level security;
alter table public.tickets force row level security;

-- Foreign key indexes (schema-foreign-key-indexes.md)
create index tickets_event_id_status_idx on public.tickets(event_id, status);
create index tickets_user_id_idx on public.tickets(user_id);

-- Tickets Security Policies (Optimized cache patterns)
create policy view_own_tickets on public.tickets
  for select to authenticated using (user_id = (select auth.uid()));

create policy view_organizer_tickets on public.tickets
  for select to authenticated using (
    exists (
      select 1 from public.events 
      where events.id = tickets.event_id and events.organizer_id = (select auth.uid())
    )
  );

create policy book_ticket on public.tickets
  for insert to authenticated with check (user_id = (select auth.uid()));

create policy cancel_own_ticket on public.tickets
  for update to authenticated using (user_id = (select auth.uid())) with check (status = 'CANCELLED');


-- -------------------------------------------------------------------------
-- 4. STAFF ASSIGNMENTS TABLE
-- -------------------------------------------------------------------------
create table public.staff_assignments (
  id uuid default gen_random_uuid() primary key,
  event_id uuid references public.events(id) on delete cascade not null,
  user_id uuid references public.profiles(id) on delete cascade not null,
  assigned_at timestamptz default now() not null,
  constraint unique_event_staff unique(event_id, user_id)
);

-- Enable RLS on staff_assignments
alter table public.staff_assignments enable row level security;
alter table public.staff_assignments force row level security;

-- Foreign key indexes (schema-foreign-key-indexes.md)
create index staff_assignments_event_id_idx on public.staff_assignments(event_id);
create index staff_assignments_user_id_idx on public.staff_assignments(user_id);

-- Staff Assignments Security Policies
create policy view_own_staff_assignments on public.staff_assignments
  for select to authenticated using (user_id = (select auth.uid()));

create policy manage_staff_assignments on public.staff_assignments
  for all to authenticated using (
    exists (
      select 1 from public.events
      where events.id = staff_assignments.event_id and events.organizer_id = (select auth.uid())
    )
  );


-- -------------------------------------------------------------------------
-- 5. CHECK-INS TABLE
-- -------------------------------------------------------------------------
create table public.checkins (
  id uuid default gen_random_uuid() primary key,
  ticket_id uuid references public.tickets(id) on delete cascade unique not null,
  validated_by uuid references public.profiles(id) on delete set null,
  checkin_time timestamptz default now() not null
);

-- Enable RLS on checkins
alter table public.checkins enable row level security;
alter table public.checkins force row level security;

-- Foreign key indexes (schema-foreign-key-indexes.md)
create index checkins_validated_by_idx on public.checkins(validated_by);

-- Check-ins Security Policies
create policy view_own_checkin on public.checkins
  for select to authenticated using (
    exists (
      select 1 from public.tickets
      where tickets.id = checkins.ticket_id and tickets.user_id = (select auth.uid())
    )
  );

create policy manage_event_checkins on public.checkins
  for all to authenticated using (
    exists (
      select 1 from public.tickets
      join public.events on events.id = tickets.event_id
      where tickets.id = checkins.ticket_id 
      and (
        events.organizer_id = (select auth.uid())
        or exists (
          select 1 from public.staff_assignments sa
          where sa.event_id = events.id and sa.user_id = (select auth.uid())
        )
      )
    )
  );
