-- Niyam OTA remote config + content (SP-P-OTA). Run once in Supabase → SQL Editor.
--
-- app_config: public-readable key/value config the app pulls at launch so you
-- can change content/behaviour WITHOUT shipping a build. Anyone may READ (the
-- app reads it before sign-in too); only the dashboard / service_role may write
-- (no insert/update/delete policy is granted, and service_role bypasses RLS).
-- Every value has a hardcoded fallback in the app, so a missing/empty row can
-- never brick it. None of this touches the blocking engine.

create table if not exists public.app_config (
  key text primary key,
  value jsonb not null,
  updated_at timestamptz default now()
);

alter table public.app_config enable row level security;

drop policy if exists "public read config" on public.app_config;
create policy "public read config" on public.app_config
  for select using (true);

-- Seed with the values this build ships with, so remote == build at launch.
insert into public.app_config (key, value) values
  ('free_mantra_ids',
   '["gita-2-47","mahamrityunjaya","gayatri","gita-4-7-8","hanuman-chalisa-opening"]'::jsonb),
  ('blockable_apps',
   '[
      {"name":"Instagram","pkg":"com.instagram.android","slug":"instagram"},
      {"name":"YouTube","pkg":"com.google.android.youtube","slug":"youtube"},
      {"name":"Facebook","pkg":"com.facebook.katana","slug":"facebook"},
      {"name":"X (formerly Twitter)","pkg":"com.twitter.android","slug":"x"},
      {"name":"Reddit","pkg":"com.reddit.frontpage","slug":"reddit"},
      {"name":"Snapchat","pkg":"com.snapchat.android","slug":"snapchat"},
      {"name":"TikTok","pkg":"com.zhiliaoapp.musically","slug":"tiktok"},
      {"name":"Free Fire","pkg":"com.dts.freefireth","slug":"freefire"},
      {"name":"Call of Duty Mobile","pkg":"com.activision.callofduty.shooter","slug":"codmobile"},
      {"name":"Candy Crush Saga","pkg":"com.king.candycrushsaga","slug":"candycrush"},
      {"name":"Ludo King","pkg":"com.ludo.king","slug":"ludoking"}
    ]'::jsonb),
  ('paywall',
   '{"weekly":15,"monthly":49,"yearly":399,"trust":"Cancel anytime in Google Play"}'::jsonb),
  ('announcement',
   '{"active":false,"title":"","body":""}'::jsonb),
  ('feature_flags',
   '{"ads_enabled":true}'::jsonb),
  ('min_supported_version_code', '0'::jsonb),
  ('update_message',
   '"A new version of Niyam is available. Please update to keep practising."'::jsonb),
  ('content_version', '0'::jsonb)
on conflict (key) do nothing;

-- Public Storage bucket for the mantra catalog (OTA content). Upload a new
-- content/mantras.json here and bump app_config.content_version to push it.
insert into storage.buckets (id, name, public)
  values ('content', 'content', true)
  on conflict (id) do nothing;
