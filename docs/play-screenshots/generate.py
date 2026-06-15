#!/usr/bin/env python3
"""Generate Play Store marketing screenshots for Niyam, using a REAL device mockup.

Pipeline:
  1. Composite each real app screenshot into the supplied phone mockup
     (mockup.png) — white screen knocked out to a transparent hole so the
     screenshot shows through, dark frame + punch-hole camera kept on top.
  2. Drop that real device + the brand lockup into branded marketing frames
     (sunrise / bottle-green) following the reference composition.
  3. Emit one self-contained HTML per frame; the sibling shell step renders
     each to PNG via headless Chrome at exact Play sizes.
"""
import base64
import pathlib
from PIL import Image, ImageDraw

ROOT = pathlib.Path(__file__).resolve().parent
SHOTS = ROOT.parent / "screenshots" / "v2"
LOGO = ROOT.parent.parent / "website" / "assets" / "lockup.png"
DEV = ROOT / "devices"; DEV.mkdir(exist_ok=True)
OUT = ROOT / "html"; OUT.mkdir(exist_ok=True)

# ---- 1. composite screenshots into the real mockup ------------------------
mock = Image.open(ROOT / "mockup.png").convert("RGBA")
W, H = mock.size

flood = mock.copy().convert("RGB")
ImageDraw.floodfill(flood, (W // 2, H // 2), (255, 0, 255), thresh=40)
fp = flood.load()
xs, ys = [], []
for y in range(H):
    row = False
    for x in range(W):
        if fp[x, y] == (255, 0, 255):
            xs.append(x); ys.append(y)
L, T, R, B = min(xs), min(ys), max(xs), max(ys)
SW, SH = R - L, B - T

# frame layer: near-white (screen + exterior) -> transparent; dark frame stays
frame = mock.copy()
px = frame.load()
for y in range(H):
    for x in range(W):
        r, g, b, a = px[x, y]
        if r > 238 and g > 238 and b > 238:
            px[x, y] = (r, g, b, 0)

def cover(img, tw, th):
    iw, ih = img.size
    s = max(tw / iw, th / ih)
    nimg = img.resize((round(iw * s), round(ih * s)), Image.LANCZOS)
    nw, nh = nimg.size
    l = (nw - tw) // 2
    return nimg.crop((l, 0, l + tw, th))  # top-align

SHOTMAP = {
    "1-home": "08-home.png", "2-overlay": "09-overlay.png",
    "3-detail": "12-detail.png", "4-library": "10-library.png",
    "5-streak": "15-home-dark.png",
}
device_b64 = {}
for slug, fn in SHOTMAP.items():
    scr = cover(Image.open(SHOTS / fn).convert("RGBA"), SW, SH)
    canvas = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    canvas.paste(scr, (L, T))
    canvas = Image.alpha_composite(canvas, frame)
    canvas = canvas.crop(canvas.getbbox())
    out = DEV / f"{slug}.png"
    canvas.save(out)
    device_b64[slug] = "data:image/png;base64," + base64.b64encode(out.read_bytes()).decode()
    print("device", slug, canvas.size)

logo_b64 = "data:image/png;base64," + base64.b64encode(LOGO.read_bytes()).decode()

# ---- 2. marketing frames ---------------------------------------------------
# (slug, theme, overline, headline, subtitle)
FRAMES = [
    ("1-home", "sunrise", None, "A pause before the scroll.",
     "Your mantra appears before the app does.", True),   # True = show top logo
    ("2-overlay", "green", "THE CORE MOMENT", "The mantra comes first.",
     "Fifteen seconds of stillness before any distracting app opens.", False),
    ("3-detail", "sunrise", "26 AUTHENTIC MANTRAS", "In your own language.",
     "Sanskrit, transliteration and meaning — across nine languages.", False),
    ("4-library", "green", "A LIVING LIBRARY", "Choose your practice.",
     "Browse by intention, deity, or length.", False),
    ("5-streak", "green", "DAILY SADHANA", "One mantra at a time.",
     "Build a quiet daily rhythm and watch your streak grow.", False),
    ("6-closer", "closer", None, "Begin your sadhana.", None, False),
]

THEMES = {
    "sunrise": dict(bg="radial-gradient(120% 90% at 50% 0%, #FFF6EC 0%, #FFE4C7 55%, #FFD2A6 100%)",
                    head="#0E3B2C", over="#C0560F", sub="#5E4632", dot="rgba(255,100,0,.16)"),
    "green": dict(bg="radial-gradient(120% 90% at 50% 0%, #18553F 0%, #0F3D2E 60%, #0A2C21 100%)",
                  head="#FBF3E7", over="#FFB066", sub="#CFE3D7", dot="rgba(255,140,60,.18)"),
    "closer": dict(bg="radial-gradient(130% 100% at 50% 30%, #FFF6EC 0%, #FFE4C7 60%, #FFCF9F 100%)",
                   head="#0E3B2C", over="#C0560F", sub="#5E4632", dot="rgba(255,100,0,.14)"),
}

PAGE = """<!DOCTYPE html><html><head><meta charset="utf-8">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,500&family=Outfit:wght@400;500;600&display=swap" rel="stylesheet">
<style>
  * {{ margin:0; padding:0; box-sizing:border-box; }}
  html,body {{ width:100vw; height:100vh; overflow:hidden; }}
  .root {{ width:100vw; height:100vh; background:{bg}; position:relative; overflow:hidden;
           display:flex; flex-direction:column; align-items:center; font-family:'Outfit',sans-serif; }}
  .blob {{ position:absolute; border-radius:50%; filter:blur(40px); background:{dot}; }}
  .b1 {{ width:46vw; height:46vw; top:-12vw; right:-10vw; }}
  .b2 {{ width:34vw; height:34vw; bottom:4vw; left:-12vw; }}
  .text {{ width:100%; padding:8.5vh 9vw 0; text-align:center; z-index:2; }}
  .toplogo {{ height:7vh; margin-bottom:3.4vh; }}
  .over {{ font-size:2.7vw; letter-spacing:.32em; font-weight:600; color:{over}; margin-bottom:2.4vh; }}
  h1 {{ font-family:'Fraunces',serif; font-weight:400; font-size:7.4vw; line-height:1.06;
        color:{head}; letter-spacing:-.01em; }}
  .sub {{ font-size:3.35vw; color:{sub}; margin-top:2.6vh; line-height:1.4; padding:0 2vw; }}
  .device {{ width:67vw; margin-top:5vh; filter:drop-shadow(0 3vw 6vw rgba(20,10,0,.34)); z-index:2; display:block; }}
  .closer {{ flex:1; display:flex; flex-direction:column; align-items:center; justify-content:center;
             gap:3vh; z-index:2; margin-top:-4vh; }}
  .closer img {{ width:52vw; }}
  .pron {{ font-size:3vw; letter-spacing:.05em; color:{sub}; }}
</style></head><body>
<div class="root">
  <div class="blob b1"></div><div class="blob b2"></div>
  {body}
</div></body></html>"""

def build(slug, theme, over, head, sub, toplogo):
    t = THEMES[theme]
    if theme == "closer":
        body = f"""<div class="closer">
            <img src="{logo_b64}" alt="Niyam">
            <h1 style="font-size:8vw">{head}</h1>
            <div class="pron">pronounced &nbsp;NEE-yum</div></div>"""
    else:
        tl = f'<img class="toplogo" src="{logo_b64}">' if toplogo else ''
        ov = f'<div class="over">{over}</div>' if over else ''
        sb = f'<div class="sub">{sub}</div>' if sub else ''
        body = f'<div class="text">{tl}{ov}<h1>{head}</h1>{sb}</div><img class="device" src="{device_b64[slug]}">'
    html = PAGE.format(bg=t["bg"], over=t["over"], head=t["head"], sub=t["sub"], dot=t["dot"], body=body)
    (OUT / f"{slug}.html").write_text(html, encoding="utf-8")
    print("wrote", slug)

for slug, theme, over, head, sub, toplogo in FRAMES:
    build(slug, theme, over, head, sub, toplogo)
print("done")
