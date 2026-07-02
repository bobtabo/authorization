#!/bin/bash
# Playwright が出力した .webm を ffmpeg で GIF に変換する
#
# 使い方:
#   bash scripts/video-to-gif.sh <input.webm> <output.gif>
#
# 前提:
#   - ffmpeg がインストール済みであること
#
# オプション:
#   fps=10, 幅=1280px 固定、無限ループ

set -euo pipefail

if [ $# -lt 2 ]; then
  echo "Usage: $0 <input.webm> <output.gif>" >&2
  exit 1
fi

INPUT="$1"
OUTPUT="$2"

if [ ! -f "$INPUT" ]; then
  echo "Error: input file not found: $INPUT" >&2
  exit 1
fi

ffmpeg -i "$INPUT" \
  -vf "fps=10,scale=1280:-1:flags=lanczos,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse" \
  -loop 0 "$OUTPUT"

echo "GIF generated: $OUTPUT"
