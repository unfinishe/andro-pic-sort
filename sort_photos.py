#!/usr/bin/env python3
"""Wrapper to run sort_photos from the root directory."""

import sys
from pathlib import Path

# Add src to path
src_path = Path(__file__).parent / "src"
sys.path.insert(0, str(src_path))

from sort_photos import main

if __name__ == "__main__":
    main()
