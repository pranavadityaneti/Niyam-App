// Niyam — delete-account Edge Function (SP-P3b).
//
// Permanently deletes the calling user's auth account. The four per-user
// tables (profiles / practice_state / favourites / entitlements) cascade-delete
// via their `references auth.users(id) on delete cascade` FKs, so deleting the
// auth user removes everything we hold for them.
//
// Security model:
//   - The caller proves identity with their session JWT in the Authorization
//     header. We resolve that JWT to a user with an ANON-key client.
//   - Deletion itself needs admin rights, so we use a separate SERVICE_ROLE
//     client. The service_role key lives ONLY in the function's environment
//     (auto-injected by Supabase as SUPABASE_SERVICE_ROLE_KEY) — never in the app.
//   - A user can therefore only ever delete THEIR OWN account: the id we delete
//     is the one resolved from their own token, not anything they send us.
//
// Deploy:  supabase functions deploy delete-account
// (No extra secrets to set — SUPABASE_URL / ANON / SERVICE_ROLE are provided.)

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return json({ error: "Missing Authorization header" }, 401);
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

    // Resolve the caller's identity from their own JWT.
    const userClient = createClient(supabaseUrl, anonKey, {
      global: { headers: { Authorization: authHeader } },
    });
    const { data: userData, error: userErr } = await userClient.auth.getUser();
    if (userErr || !userData?.user) {
      return json({ error: "Invalid or expired session" }, 401);
    }

    // Delete that exact user with admin rights. Tables cascade.
    const adminClient = createClient(supabaseUrl, serviceRoleKey);
    const { error: delErr } = await adminClient.auth.admin.deleteUser(userData.user.id);
    if (delErr) {
      return json({ error: delErr.message }, 500);
    }

    return json({ deleted: true }, 200);
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
