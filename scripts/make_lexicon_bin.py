#!/usr/bin/env python3
"""
Create an exact 5-letter Persian lexicon binary for on-device, 100% accurate lookup.

Output format (words_5_be.bin):
- 4 bytes: big-endian unsigned count (N)
- N records: each record is 10 bytes = 5 UTF-16BE code units (no BOM)

Usage examples:
1) From a text list (one word per line, UTF-8):
   python scripts/make_lexicon_bin.py --input words_for_bloom.txt --output composeApp/src/androidMain/assets/words_5_be.bin

2) From your rich JSON (same as extract_words_for_bloom.py expects):
   python scripts/make_lexicon_bin.py --json persian_packs_v3.json --output composeApp/src/androidMain/assets/words_5_be.bin

Notes:
- Normalization matches the app (WordChecker.normalizePersian)
- Only words that are exactly 5 letters after normalization are kept
- Words are lowercased, normalized, deduplicated, sorted (codepoint order)
"""

import argparse
import json
from pathlib import Path

# Normalization identical to app (WordChecker.normalizePersian)
SUBS_MAP = {
    0x200C: '',   # ZWNJ
    0x200F: '',   # RTL mark
    0x200E: '',   # LTR mark
    0x064A: 0x06CC,  # ARABIC YEH -> FARSI YEH
    0x0643: 0x06A9,  # ARABIC KAF -> KEHEH
    0x0629: 0x0647,  # TEH MARBUTA -> HEH
}
REMOVE_CPS = [
    0x0670, 0x064B, 0x064C, 0x064D, 0x064E, 0x064F, 0x0650, 0x0651, 0x0652,
    0x0653, 0x0654, 0x0655,
]


def normalize(word: str) -> str:
    s = word.strip()
    for k, v in SUBS_MAP.items():
        s = s.replace(chr(k), '' if v == '' else chr(v))
    for cp in REMOVE_CPS:
        s = s.replace(chr(cp), '')
    return s.lower()


def load_words_from_text(path: Path) -> list[str]:
    words: set[str] = set()
    with path.open('r', encoding='utf-8') as f:
        for line in f:
            w = normalize(line)
            if len(w) == 5:
                words.add(w)
    return sorted(words)


def load_words_from_json(path: Path) -> list[str]:
    words: set[str] = set()
    data = json.loads(path.read_text(encoding='utf-8'))
    for entry in data:
        raw = str(entry.get('word', '')).strip()
        w = normalize(raw)
        if len(w) == 5:
            words.add(w)
    return sorted(words)


def write_lexicon_bin(words: list[str], out_path: Path) -> None:
    # Validate all words are exactly 5 chars
    for w in words:
        if len(w) != 5:
            raise ValueError(f"Word not 5 letters after normalization: {w!r}")

    N = len(words)
    # Header: 4-byte BE count
    blob = bytearray()
    blob.extend(((N >> 24) & 0xFF).to_bytes(1, 'big'))
    blob.extend(((N >> 16) & 0xFF).to_bytes(1, 'big'))
    blob.extend(((N >> 8) & 0xFF).to_bytes(1, 'big'))
    blob.extend((N & 0xFF).to_bytes(1, 'big'))

    # Records: 5 UTF-16BE code units (10 bytes) each
    for w in words:
        for ch in w:
            code = ord(ch)
            if code > 0xFFFF:
                raise ValueError(f"Non-BMP char not supported: {ch} (U+{code:04X})")
            blob.append((code >> 8) & 0xFF)
            blob.append(code & 0xFF)

    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_bytes(bytes(blob))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--input', type=Path, help='Text file with one word per line (UTF-8)')
    ap.add_argument('--json', type=Path, help='Rich JSON with entries containing "word"')
    ap.add_argument('--output', type=Path, required=True, help='Output path for words_5_be.bin')
    args = ap.parse_args()

    if not args.input and not args.json:
        ap.error('Provide either --input or --json')

    if args.input:
        words = load_words_from_text(args.input)
    else:
        words = load_words_from_json(args.json)

    write_lexicon_bin(words, args.output)
    size_kb = args.output.stat().st_size / 1024
    print(f"✅ Wrote {len(words):,} words to {args.output} ({size_kb:.1f} KB)")


if __name__ == '__main__':
    main()


