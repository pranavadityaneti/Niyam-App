// Niyam — verify-entitlement Edge Function (SP-P5c).
//
// Verifies a Google Play subscription purchase server-side and writes the
// trusted `entitlements` row. The app NEVER decides premium on its own from
// here on: it sends the purchase token, we ask Google, and we record the answer
// under the caller's account. Every device that signs in then reads that row.
//
// Security model (same spine as delete-account):
//   - Caller identity comes ONLY from their session JWT (anon client → getUser).
//     We write the row for THAT user id, never one sent in the body.
//   - Google is authenticated with a service-account key held as the Supabase
//     secret GOOGLE_PLAY_SA_KEY (a JSON string). It is NOT the service_role key
//     and never ships in the app.
//   - The entitlements table is service-role-write-only (RLS); we use the
//     service_role client (auto-injected) to upsert.
//
// Input  (POST JSON): { productId: string, purchaseToken: string }
// Output (200 JSON):  { premium_active: boolean, premium_plan: string | null }
//
// Setup (Pranav):
//   supabase secrets set GOOGLE_PLAY_SA_KEY="$(cat play-service-account.json)"
//   supabase functions deploy verify-entitlement

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const PACKAGE_NAME = "com.myniyam.app";
const ANDROID_PUBLISHER = "https://androidpublisher.googleapis.com/androidpublisher/v3";
const SCOPE = "https://www.googleapis.com/auth/androidpublisher";

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

    // 1. Who is calling?
    const userClient = createClient(supabaseUrl, anonKey, {
      global: { headers: { Authorization: authHeader } },
    });
    const { data: userData, error: userErr } = await userClient.auth.getUser();
    if (userErr || !userData?.user) return json({ error: "Invalid or expired session" }, 401);
    const userId = userData.user.id;

    // 2. Validate input.
    const body = await req.json().catch(() => null);
    const productId: string | undefined = body?.productId;
    const purchaseToken: string | undefined = body?.purchaseToken;
    if (!productId || !purchaseToken) {
      return json({ error: "productId and purchaseToken are required" }, 400);
    }

    // 3. Ask Google about the subscription.
    const accessToken = await getGoogleAccessToken();
    const url =
      `${ANDROID_PUBLISHER}/applications/${PACKAGE_NAME}/purchases/subscriptionsv2/tokens/${encodeURIComponent(purchaseToken)}`;
    const gRes = await fetch(url, { headers: { Authorization: `Bearer ${accessToken}` } });
    if (!gRes.ok) {
      console.error("Play verification failed", gRes.status, await gRes.text());
      return json({ error: "Could not verify purchase" }, 502);
    }
    const sub = await gRes.json();

    // 4. Decide entitlement.
    const state: string = sub.subscriptionState ?? "";
    const lineItems: Array<{ productId?: string; expiryTime?: string }> = sub.lineItems ?? [];
    const now = Date.now();
    const hasFutureExpiry = lineItems.some(
      (li) => li.expiryTime && Date.parse(li.expiryTime) > now,
    );
    const entitled =
      state === "SUBSCRIPTION_STATE_ACTIVE" ||
      state === "SUBSCRIPTION_STATE_IN_GRACE_PERIOD" ||
      (state === "SUBSCRIPTION_STATE_CANCELED" && hasFutureExpiry);
    // Only these states are a DEFINITIVE "no longer entitled" — safe to revoke.
    // Ambiguous states (ON_HOLD, PAUSED, PENDING, unknown) must NOT flip a paid
    // user to false, or a transient Play state would wrongly lock them out.
    const terminal =
      state === "SUBSCRIPTION_STATE_EXPIRED" ||
      state === "SUBSCRIPTION_STATE_REVOKED" ||
      (state === "SUBSCRIPTION_STATE_CANCELED" && !hasFutureExpiry);

    // Plan strictly from Google's line item — never trust the client-sent productId.
    const planProductId = lineItems.find((li) => li.productId)?.productId ?? null;
    if (entitled && !planProductId) {
      console.error("verify-entitlement: entitled sub has no line-item productId");
      return json({ error: "Verification incomplete" }, 502);
    }

    const admin = createClient(supabaseUrl, serviceRoleKey);

    // 5. Bind the purchase token to ONE account — stops a single subscription from
    // unlocking unlimited accounts. First account to redeem a token owns it.
    const { data: tokenOwner } = await admin
      .from("entitlements")
      .select("user_id")
      .eq("purchase_token", purchaseToken)
      .maybeSingle();
    if (tokenOwner && tokenOwner.user_id !== userId) {
      return json({ error: "This purchase is linked to another account" }, 409);
    }

    // 6. Write the trusted row (service role; user can only read it).
    if (entitled) {
      const { error: upErr } = await admin.from("entitlements").upsert(
        {
          user_id: userId,
          premium_active: true,
          premium_plan: planProductId,
          purchase_token: purchaseToken,
          source: "play",
          updated_at: new Date().toISOString(),
        },
        { onConflict: "user_id" },
      );
      if (upErr) { console.error(upErr.message); return json({ error: "Could not record entitlement" }, 500); }
      return json({ premium_active: true, premium_plan: planProductId }, 200);
    } else if (terminal) {
      const { error: upErr } = await admin.from("entitlements").upsert(
        {
          user_id: userId,
          premium_active: false,
          premium_plan: null,
          purchase_token: purchaseToken,
          source: "play",
          updated_at: new Date().toISOString(),
        },
        { onConflict: "user_id" },
      );
      if (upErr) { console.error(upErr.message); return json({ error: "Could not record entitlement" }, 500); }
      return json({ premium_active: false, premium_plan: null }, 200);
    } else {
      // Ambiguous Play state — do NOT touch the premium flag.
      return json({ premium_active: false, premium_plan: null, ambiguous: true }, 200);
    }
  } catch (e) {
    console.error("verify-entitlement error", e);
    return json({ error: "Internal error" }, 500);
  }
});

// --- Google service-account auth (Web Crypto, no external deps) --------------

async function getGoogleAccessToken(): Promise<string> {
  const raw = Deno.env.get("GOOGLE_PLAY_SA_KEY");
  if (!raw) throw new Error("GOOGLE_PLAY_SA_KEY secret is not set");
  const sa = JSON.parse(raw) as {
    client_email: string;
    private_key: string;
    token_uri?: string;
  };
  const tokenUri = sa.token_uri ?? "https://oauth2.googleapis.com/token";

  const iat = Math.floor(Date.now() / 1000);
  const exp = iat + 3600;
  const header = { alg: "RS256", typ: "JWT" };
  const claim = { iss: sa.client_email, scope: SCOPE, aud: tokenUri, iat, exp };

  const enc = (obj: unknown) => b64url(new TextEncoder().encode(JSON.stringify(obj)));
  const unsigned = `${enc(header)}.${enc(claim)}`;

  const key = await crypto.subtle.importKey(
    "pkcs8",
    pemToDer(sa.private_key),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"],
  );
  const sig = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    key,
    new TextEncoder().encode(unsigned),
  );
  const jwt = `${unsigned}.${b64url(new Uint8Array(sig))}`;

  const res = await fetch(tokenUri, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  });
  if (!res.ok) throw new Error(`Google token exchange failed: ${res.status} ${await res.text()}`);
  const tok = await res.json();
  return tok.access_token as string;
}

function b64url(bytes: Uint8Array): string {
  let bin = "";
  for (const b of bytes) bin += String.fromCharCode(b);
  return btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function pemToDer(pem: string): ArrayBuffer {
  const body = pem
    .replace(/-----BEGIN PRIVATE KEY-----/, "")
    .replace(/-----END PRIVATE KEY-----/, "")
    .replace(/\s+/g, "");
  const bin = atob(body);
  const buf = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) buf[i] = bin.charCodeAt(i);
  return buf.buffer;
}

function json(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
