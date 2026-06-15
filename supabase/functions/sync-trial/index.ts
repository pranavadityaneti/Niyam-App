// Niyam — sync-trial Edge Function (SP-P5c-4).
//
// Records the user's 7-day trial start server-side so a reinstall can't reset
// it (the trial-reinstall loophole). Earliest-start-wins: if the server already
// has an earlier start, that one is kept and returned, so a fresh install that
// sends "today" can never push the trial later.
//
// Why a function (not a direct client write): the `entitlements` table is
// service-role-write-only (RLS). This keeps the app unable to forge entitlement
// while still letting it report when its trial began.
//
// Input  (POST JSON): { trialStartEpochDay: number }
// Output (200 JSON):  { trial_start_epoch_day: number }
//
// Deploy (Pranav): supabase functions deploy sync-trial

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) return json({ error: "Missing Authorization header" }, 401);

    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

    const userClient = createClient(supabaseUrl, anonKey, {
      global: { headers: { Authorization: authHeader } },
    });
    const { data: userData, error: userErr } = await userClient.auth.getUser();
    if (userErr || !userData?.user) return json({ error: "Invalid or expired session" }, 401);
    const userId = userData.user.id;

    const body = await req.json().catch(() => null);
    const incoming = Number(body?.trialStartEpochDay);
    if (!Number.isFinite(incoming) || incoming <= 0) {
      return json({ error: "trialStartEpochDay must be a positive number" }, 400);
    }

    const admin = createClient(supabaseUrl, serviceRoleKey);

    // Earliest non-zero start wins.
    const { data: existing } = await admin
      .from("entitlements")
      .select("trial_start_epoch_day")
      .eq("user_id", userId)
      .maybeSingle();

    const existingStart = existing?.trial_start_epoch_day ?? 0;
    const effective = existingStart > 0 ? Math.min(existingStart, incoming) : incoming;

    const { error: upErr } = await admin.from("entitlements").upsert(
      {
        user_id: userId,
        trial_start_epoch_day: effective,
        source: "trial",
        updated_at: new Date().toISOString(),
      },
      { onConflict: "user_id" },
    );
    if (upErr) return json({ error: upErr.message }, 500);

    return json({ trial_start_epoch_day: effective }, 200);
  } catch (e) {
    return json({ error: String(e) }, 500);
  }
});

function json(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
