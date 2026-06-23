-- Niyam — bind a Google Play purchase token to a single account (audit fix).
-- Run once in Supabase → SQL Editor. Without this, one paid subscription token
-- could be replayed to grant premium to unlimited accounts. verify-entitlement
-- now records the token and rejects it if another account already owns it.

alter table public.entitlements
  add column if not exists purchase_token text;

-- One token → at most one entitlements row. Partial index so the many trial
-- rows (NULL token) are unaffected.
create unique index if not exists entitlements_purchase_token_key
  on public.entitlements (purchase_token)
  where purchase_token is not null;
