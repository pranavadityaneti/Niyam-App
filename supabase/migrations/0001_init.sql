-- Niyam backend schema (SP-P2). Run once in Supabase → SQL Editor.
-- Four per-user tables, all protected by Row-Level Security so a user can only
-- read/write their own rows. The service_role key (server-only) bypasses RLS;
-- the app uses the anon key + the user's session JWT.

-- 1. profiles ---------------------------------------------------------------
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text,
  created_at timestamptz default now()
);

-- 2. practice_state (one synced snapshot per user) --------------------------
create table if not exists public.practice_state (
  user_id uuid primary key references auth.users(id) on delete cascade,
  current_mantra_id text,
  sadhana_start_epoch_day bigint,
  completed_mantra_ids text[] default '{}',
  selected_intention text,
  display_language text,
  streak_count int default 0,
  updated_at timestamptz default now()
);

-- 3. favourites (one row per user+mantra) -----------------------------------
create table if not exists public.favourites (
  user_id uuid references auth.users(id) on delete cascade,
  mantra_id text not null,
  created_at timestamptz default now(),
  primary key (user_id, mantra_id)
);

-- 4. entitlements (server-trusted; written by Edge Function in P5) ----------
create table if not exists public.entitlements (
  user_id uuid primary key references auth.users(id) on delete cascade,
  premium_active boolean default false,
  premium_plan text,
  trial_start_epoch_day bigint,
  source text,
  updated_at timestamptz default now()
);

-- Row-Level Security --------------------------------------------------------
alter table public.profiles       enable row level security;
alter table public.practice_state enable row level security;
alter table public.favourites     enable row level security;
alter table public.entitlements   enable row level security;

create policy "own profile" on public.profiles
  for all using (auth.uid() = id) with check (auth.uid() = id);
create policy "own state" on public.practice_state
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "own favs" on public.favourites
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
-- entitlements: user may READ own; writes restricted to service role (Edge Function in P5).
create policy "read own entitlements" on public.entitlements
  for select using (auth.uid() = user_id);

-- Auto-create a profile row when a new auth user signs up ------------------
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = '' as $$
begin
  insert into public.profiles (id, email)
  values (new.id, new.email)
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();
