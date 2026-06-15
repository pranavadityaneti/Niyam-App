#!/usr/bin/env python3
"""Generate Play Store marketing screenshots (phone + tablet) for Niyam.

Composes real app screenshots into branded marketing frames (sunrise / bottle-green),
following the reference composition (bold bg + device mockup + headline). Emits one
self-contained HTML per frame; a sibling shell step renders each to PNG via headless
Chrome at exact Play dimensions. CSS uses vw/vh units so it renders crisp at any size.
"""
import base64
import os
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent
SHOTS = ROOT.parent / "screenshots" / "v2"
OUT = ROOT / "html"
OUT.mkdir(exist_ok=True)

def b64(name):
    p = SHOTS / name
    return "data:image/png;base64," + base64.b64encode(p.read_bytes()).decode()

# (slug, image-or-None, theme, overline, headline, subtitle)
FRAMES = [
    ("1-home", "08-home.png", "sunrise", "NIYAM",
     "A pause before the scroll.",
     "Your mantra appears before the app does."),
    ("2-overlay", "09-overlay.png", "green", "THE CORE MOMENT",
     "The mantra comes first.",
     "Fifteen seconds of stillness before any distracting app opens."),
    ("3-detail", "12-detail.png", "sunrise", "26 AUTHENTIC MANTRAS",
     "In your own language.",
     "Sanskrit, transliteration and meaning — across nine languages."),
    ("4-library", "10-library.png", "green", "A LIVING LIBRARY",
     "Choose your practice.",
     "Browse by intention, deity, or length."),
    ("5-streak", "15-home-dark.png", "sunrise", "DAILY SADHANA",
     "One mantra at a time.",
     "Build a quiet daily rhythm and watch your streak grow."),
    ("6-closer", None, "green-solid", None,
     "Begin your sadhana.",
     None),
]

THEMES = {
    "sunrise": dict(
        bg="radial-gradient(120% 90% at 50% 0%, #FFF6EC 0%, #FFE4C7 55%, #FFD2A6 100%)",
        head="#0E3B2C", over="#C0560F", sub="#5E4632", dot="rgba(255,100,0,.16)"),
    "green": dict(
        bg="radial-gradient(120% 90% at 50% 0%, #18553F 0%, #0F3D2E 60%, #0A2C21 100%)",
        head="#FBF3E7", over="#FFB066", sub="#CFE3D7", dot="rgba(255,140,60,.18)"),
    "green-solid": dict(
        bg="radial-gradient(130% 100% at 50% 35%, #18553F 0%, #0F3D2E 55%, #08231A 100%)",
        head="#FBF3E7", over="#FFB066", sub="#CFE3D7", dot="rgba(255,140,60,.16)"),
}

def device_html(img):
    if not img:
        return """
        <div class="closer">
          <div class="wordmark">niyam</div>
          <div class="deva">नियम</div>
          <div class="pron">pronounced &nbsp;NEE-yum</div>
          <svg class="smile" viewBox="0 0 120 40"><path d="M8 8 Q60 46 112 8" fill="none"
            stroke="#FF8A3C" stroke-width="7" stroke-linecap="round"/></svg>
        </div>"""
    return f"""
        <div class="phone"><div class="screen"><img src="{b64(img)}" alt=""></div></div>"""

PAGE = """<!DOCTYPE html><html><head><meta charset="utf-8">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,500&family=Outfit:wght@400;500;600&display=swap" rel="stylesheet">
<style>
  * {{ margin:0; padding:0; box-sizing:border-box; }}
  html,body {{ width:100vw; height:100vh; overflow:hidden; }}
  .root {{ width:100vw; height:100vh; background:{bg}; position:relative; overflow:hidden;
           display:flex; flex-direction:column; align-items:center; font-family:'Outfit',sans-serif; }}
  .blob {{ position:absolute; border-radius:50%; filter:blur(40px); background:{dot}; }}
  .b1 {{ width:46vw; height:46vw; top:-12vw; right:-10vw; }}
  .b2 {{ width:34vw; height:34vw; bottom:6vw; left:-12vw; }}
  .text {{ width:100%; padding:9.5vh 9vw 0; text-align:center; z-index:2; }}
  .over {{ font-size:2.7vw; letter-spacing:.32em; font-weight:600; color:{over}; margin-bottom:2.4vh; }}
  h1 {{ font-family:'Fraunces',serif; font-weight:400; font-size:7.4vw; line-height:1.06;
        color:{head}; letter-spacing:-.01em; }}
  .sub {{ font-size:3.35vw; color:{sub}; margin-top:2.6vh; line-height:1.4; font-weight:400;
          padding:0 2vw; }}
  .phone {{ margin-top:6.2vh; width:62vw; background:#141414; border-radius:9vw; padding:1.1vw;
            box-shadow:0 4vw 9vw rgba(20,10,0,.34); z-index:2; }}
  .screen {{ border-radius:7.9vw; overflow:hidden; max-height:60vh; background:#000; }}
  .screen img {{ width:100%; display:block; }}
  .closer {{ flex:1; display:flex; flex-direction:column; align-items:center; justify-content:center;
             gap:2.2vh; z-index:2; margin-top:-6vh; }}
  .wordmark {{ font-family:'Fraunces',serif; font-size:13vw; color:#FBF3E7; line-height:1; }}
  .deva {{ font-size:6vw; color:#FFB066; }}
  .pron {{ font-size:3vw; letter-spacing:.06em; color:#CFE3D7; }}
  .smile {{ width:18vw; margin-top:2vh; }}
</style></head><body>
<div class="root">
  <div class="blob b1"></div><div class="blob b2"></div>
  {textblock}
  {device}
</div></body></html>"""

def text_block(over, head, sub):
    o = f'<div class="over">{over}</div>' if over else ''
    s = f'<div class="sub">{sub}</div>' if sub else ''
    if head and not over and not sub:  # closer: headline lives lower, smaller top text
        return f'<div class="text" style="padding-top:11vh">{o}<h1>{head}</h1>{s}</div>'
    return f'<div class="text">{o}<h1>{head}</h1>{s}</div>'

for slug, img, theme, over, head, sub in FRAMES:
    t = THEMES[theme]
    html = PAGE.format(bg=t["bg"], over=t["over"], head=t["head"], sub=t["sub"], dot=t["dot"],
                       textblock=text_block(over, head, sub), device=device_html(img))
    (OUT / f"{slug}.html").write_text(html, encoding="utf-8")
    print(f"wrote {slug}.html")
print("done")
