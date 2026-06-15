#!/usr/bin/env python3
"""Add localized `name` and `sourceLabel` (7 scripts each) to every mantra in
mantras.json. Devanagari masters below are authored from the verified verse
text / well-known titles; the other 5 Indic scripts are derived deterministically
by aksharamukha (same engine + TARGETS as generate_scripts.py). roman is pinned
to the existing curated canonicalName / source so the English UI is unchanged.

Run:  tools/.venv/bin/python tools/generate_names.py
"""
import json
from pathlib import Path
from aksharamukha import transliterate

MANTRAS = Path(__file__).resolve().parent.parent / "app/src/main/assets/content/mantras.json"
SRC = "Devanagari"
TARGETS = {
    "telugu":   ("Telugu",   [], []),
    "tamil":    ("Tamil",    [], ["TamilRemoveApostrophe", "TamilRemoveNumbers"]),
    "kannada":  ("Kannada",  [], []),
    "bengali":  ("Bengali",  [], []),
    "gujarati": ("Gujarati", [], []),
}

# id -> Devanagari core NAME master (no English editorial descriptors)
NAME_DEVA = {
    "om": "ॐ",
    "gita-2-47": "कर्मण्येवाधिकारस्ते",
    "gita-6-5": "उद्धरेदात्मनात्मानम्",
    "gita-2-14": "मात्रास्पर्शास्तु",
    "asato-ma": "असतो मा सद्गमय",
    "gita-6-6": "बन्धुरात्मात्मनः",
    "mahamrityunjaya": "महामृत्युञ्जय मन्त्र",
    "om-sahanavavatu": "ॐ सह नाववतु",
    "om-namah-shivaya": "ॐ नमः शिवाय",
    "gita-2-70": "आपूर्यमाणम्",
    "twameva-mata": "त्वमेव माता",
    "gayatri": "गायत्री मन्त्र",
    "vakratunda": "वक्रतुण्ड महाकाय",
    "saraswati-vandana": "या कुन्देन्दुतुषारहारधवला",
    "guru-brahma": "गुरुर्ब्रह्मा गुरुर्विष्णुः",
    "hare-krishna": "हरे कृष्ण महामन्त्र",
    "gita-4-7-8": "यदा यदा हि धर्मस्य",
    "gita-18-66": "सर्वधर्मान्परित्यज्य",
    "gita-3-35": "श्रेयान्स्वधर्मो",
    "purusha-suktam": "पुरुष सूक्तम्",
    "nasadiya-suktam": "नासदीय सूक्तम्",
    "hanuman-chalisa-opening": "हनुमान चालीसा",
    "vishnu-sahasranama-opening": "विष्णु सहस्रनाम",
    "lalita-sahasranama-opening": "ललिता सहस्रनाम",
    "krishna-ashtakam": "कृष्ण अष्टकम्",
    "ram-raksha-opening": "राम रक्षा स्तोत्र",
}

# id -> Devanagari SOURCE master (scripture name; verse number kept ASCII)
SOURCE_DEVA = {
    "om": "माण्डूक्य उपनिषद्",
    "gita-2-47": "भगवद्गीता 2.47",
    "gita-6-5": "भगवद्गीता 6.5",
    "gita-2-14": "भगवद्गीता 2.14",
    "asato-ma": "बृहदारण्यक उपनिषद् 1.3.28",
    "gita-6-6": "भगवद्गीता 6.6",
    "mahamrityunjaya": "ऋग्वेद 7.59.12",
    "om-sahanavavatu": "तैत्तिरीय उपनिषद्",
    "om-namah-shivaya": "श्री रुद्रम् (यजुर्वेद)",
    "gita-2-70": "भगवद्गीता 2.70",
    "twameva-mata": "पाण्डव गीता",
    "gayatri": "ऋग्वेद 3.62.10",
    "vakratunda": "गणेश वन्दना",
    "saraswati-vandana": "सरस्वती स्तोत्रम्",
    "guru-brahma": "गुरु स्तोत्रम्",
    "hare-krishna": "कलिसन्तरण उपनिषद्",
    "gita-4-7-8": "भगवद्गीता 4.7-8",
    "gita-18-66": "भगवद्गीता 18.66",
    "gita-3-35": "भगवद्गीता 3.35",
    "purusha-suktam": "ऋग्वेद 10.90.1",
    "nasadiya-suktam": "ऋग्वेद 10.129.1",
    "hanuman-chalisa-opening": "हनुमान चालीसा — तुलसीदास",
    "vishnu-sahasranama-opening": "महाभारत, अनुशासन पर्व",
    "lalita-sahasranama-opening": "ब्रह्माण्ड पुराण",
    "krishna-ashtakam": "आदि शङ्कराचार्य",
    "ram-raksha-opening": "बुध कौशिक",
}

def to_scripts(deva: str, roman: str) -> dict:
    out = {"devanagari": deva}
    for field, (target, pre, post) in TARGETS.items():
        out[field] = transliterate.process(SRC, target, deva, pre_options=pre, post_options=post).strip()
    out["roman"] = roman  # pinned to curated English string
    return out

data = json.loads(MANTRAS.read_text(encoding="utf-8"))
missing = []
for m in data["mantras"]:
    mid = m["id"]
    if mid not in NAME_DEVA or mid not in SOURCE_DEVA:
        missing.append(mid); continue
    m["name"] = to_scripts(NAME_DEVA[mid], m["canonicalName"])
    m["sourceLabel"] = to_scripts(SOURCE_DEVA[mid], m["source"])
if missing:
    raise SystemExit(f"missing masters for: {missing}")

MANTRAS.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

# sample for eyeballing
import sys
for mid in ("gita-2-47", "hanuman-chalisa-opening", "gayatri"):
    m = next(x for x in data["mantras"] if x["id"] == mid)
    print(f"\n[{mid}]")
    print("  name.telugu   :", m["name"]["telugu"])
    print("  name.tamil    :", m["name"]["tamil"])
    print("  name.bengali  :", m["name"]["bengali"])
    print("  source.telugu :", m["sourceLabel"]["telugu"])
    print("  source.roman  :", m["sourceLabel"]["roman"])
print(f"\nPatched {len(data['mantras'])} mantras with name + sourceLabel.")
