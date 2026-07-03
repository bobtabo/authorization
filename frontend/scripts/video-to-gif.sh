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
#   fps=10、無限ループ（元動画の解像度を維持）

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
  -vf "fps=10,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse" \
  -loop 0 "$OUTPUT"

echo "GIF generated: $OUTPUT"
