# Niyam — Phase 1 Acceptance Test Report (Emulator)

> Skeleton acceptance against the 14 Phase 1 criteria from [the design spec](../specs/2026-06-09-blocking-engine-walking-skeleton-design.md#acceptance-criteria).

## Setup

- **Date tested:** _fill in when done_
- **Tester:** Pranav
- **Device:** Pixel 6 emulator (recommended), API 34, stock AOSP system image
- **APK:** `app/build/outputs/apk/debug/app-debug.apk`
- **Git commit at test:** _paste `git rev-parse HEAD` here_

## Pre-test checklist

- [ ] Open Android Studio → File → Open → select this project root (`Hindu Distraction App`)
- [ ] Wait for Gradle sync (first sync may take a couple minutes)
- [ ] Tools → Device Manager → Create Device → Pixel 6 → API 34 (Google APIs) → Finish
- [ ] Click the green ▶ "play" arrow on the emulator entry to boot it
- [ ] In the Run config dropdown, ensure `app` is selected; click ▶ Run to install + launch Niyam
- [ ] On the emulator, open Play Store; sign in with a Google account
- [ ] Install Instagram (search "Instagram", install). YouTube is preinstalled. Facebook is optional — Niyam's blocklist includes it but testing it is not strictly required for Phase 1 (criterion #4).

## Phase 1 criteria

| #  | Criterion                                                                                                                                  | Result (PASS / FAIL) | Notes |
|----|---------------------------------------------------------------------------------------------------------------------------------------------|----------------------|-------|
| 1  | APK installs and launches.                                                                                                                  |                      |       |
| 2  | Welcome → 5 permission screens → home — completable end to end with green checks on every permission.                                       |                      |       |
| 3  | User opens **Instagram** from launcher — overlay covers the screen within ~200ms.                                                            |                      |       |
| 4  | User opens **Facebook** — overlay covers the screen within ~200ms. (Skip if Facebook not installed; mark "N/A".)                              |                      |       |
| 5  | User opens **YouTube** — overlay covers the screen within ~200ms.                                                                            |                      |       |
| 6  | Overlay displays `ॐ`, "Om", "The primordial sound of the universe.", and a countdown starting at "15".                                       |                      |       |
| 7  | Countdown decrements each second.                                                                                                            |                      |       |
| 8  | "Continue" button is disabled until the countdown reaches 0.                                                                                 |                      |       |
| 9  | At 0, the button enables.                                                                                                                    |                      |       |
| 10 | Tapping the button dismisses the overlay; the blocked app is visible underneath.                                                             |                      |       |
| 11 | Re-opening the same blocked app triggers the overlay again (after the 2-second debounce).                                                    |                      |       |
| 12 | Closing the app via Recents (swipe up) does NOT prevent steps 3-11 — foreground service survives swipe-from-recents.                          |                      |       |
| 13 | Revoking AccessibilityService → blocked-app launches NOT intercepted; Home shows "Protection: At Risk" on next open.                          |                      |       |
| 14 | Opening a NON-blocked app (e.g. Chrome, Gmail) does NOT trigger the overlay.                                                                  |                      |       |

## Observations / weirdness

_Anything unexpected, e.g. overlay lag, copy errors, UI bugs you want to revisit when UI references arrive. Fine to leave blank if everything was clean._

## Verdict

- [ ] **Sub-project 1 (walking skeleton) signed off** — all 14 pass; ready to start sub-project 2 (content data model + 25 seed mantras).
- [ ] **Partial pass** — list failing criteria above; we patch before declaring done.
- [ ] **Fail** — discuss approach.
