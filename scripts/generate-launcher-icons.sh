#!/bin/bash

# Generate launcher icons from a single PNG
# Usage: ./generate-launcher-icons.sh /path/to/input.png
#
# Creates scaled versions for all mipmap densities:
# - mdpi (48×48)
# - hdpi (72×72)
# - xhdpi (96×96)
# - xxhdpi (144×144)
# - xxxhdpi (192×192)

set -e

INPUT_FILE="$1"

# Check if input file is provided
if [ -z "$INPUT_FILE" ]; then
    echo "Usage: $0 <input.png>"
    echo ""
    echo "Example: $0 ~/Downloads/my-launcher-icon.png"
    exit 1
fi

# Check if input file exists
if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: File not found: $INPUT_FILE"
    exit 1
fi

# Check if ImageMagick is installed
if ! command -v magick &> /dev/null; then
    echo "Error: ImageMagick is not installed."
    echo "Install it with: brew install imagemagick (macOS) or apt install imagemagick (Linux)"
    exit 1
fi

# Get the project root (assuming script is in scripts/ folder)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
#RES_DIR="$PROJECT_ROOT/app/src/main/res"
RES_DIR="/tmp/res"

# Define densities and sizes
declare -A SIZES=(
    ["mdpi"]="48"
    ["hdpi"]="72"
    ["xhdpi"]="96"
    ["xxhdpi"]="144"
    ["xxxhdpi"]="192"
)

echo "Generating launcher icons from: $INPUT_FILE"
echo "Output directory: $RES_DIR"
echo ""

# Generate scaled versions for each density
for density in "${!SIZES[@]}"; do
    size="${SIZES[$density]}"
    output_dir="$RES_DIR/mipmap-$density"

    # Create directory if it doesn't exist
    mkdir -p "$output_dir"

    # Generate ic_launcher.png (preserves transparency)
    echo "→ Creating $density (${size}×${size}): ic_launcher.png"
    magick "$INPUT_FILE" -background none -resize "${size}x${size}!" "$output_dir/ic_launcher.png"

    # Generate ic_launcher_round.png (same as non-round, but could be customized)
    echo "→ Creating $density (${size}×${size}): ic_launcher_round.png"
    magick "$INPUT_FILE" -background none -resize "${size}x${size}!" "$output_dir/ic_launcher_round.png"
done

echo ""
echo "✅ Done! Launcher icons generated successfully."
echo ""
echo "Files created:"
for density in "${!SIZES[@]}"; do
    echo "  • mipmap-$density/ic_launcher.png"
    echo "  • mipmap-$density/ic_launcher_round.png"
done

