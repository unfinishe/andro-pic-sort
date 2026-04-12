#!/usr/bin/env python3
"""
Android Pic Sort: Sorts photos by creation date from EXIF metadata.

Organizes photos into a Year/Month folder structure on Android devices.
Supports both English and German month names.
"""

import argparse
import logging
import os
import shutil
import sys
from datetime import datetime
from pathlib import Path
from typing import Optional

import argcomplete
import exifread
from tqdm import tqdm


# Month names in different languages
MONTH_NAMES = {
    "en": [
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    ],
    "de": [
        "Januar", "Februar", "März", "April", "Mai", "Juni",
        "Juli", "August", "September", "Oktober", "November", "Dezember"
    ]
}

# Supported image extensions
SUPPORTED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff"}

# Configure logging
def setup_logging(verbose: bool) -> logging.Logger:
    """Set up logging configuration."""
    level = logging.DEBUG if verbose else logging.INFO
    logging.basicConfig(
        level=level,
        format="%(asctime)s - %(levelname)s - %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S"
    )
    return logging.getLogger(__name__)


def get_exif_datetime(image_path: Path) -> Optional[datetime]:
    """
    Extract creation date from EXIF metadata.
    
    Returns datetime object if found, None otherwise.
    """
    try:
        with open(image_path, "rb") as file:
            tags = exifread.process_file(file, details=False)
        
        # Try common EXIF datetime tags
        datetime_tags = ["EXIF DateTimeOriginal", "Image DateTime", "EXIF DateTimeDigitized"]
        
        for tag in datetime_tags:
            if tag in tags:
                date_str = str(tags[tag])
                # EXIF datetime format: "YYYY:MM:DD HH:MM:SS"
                return datetime.strptime(date_str, "%Y:%m:%d %H:%M:%S")
    
    except Exception as e:
        logging.debug(f"Could not read EXIF from {image_path.name}: {e}")
    
    return None


def get_file_datetime(image_path: Path) -> datetime:
    """Get file modification time as fallback."""
    timestamp = os.path.getmtime(image_path)
    return datetime.fromtimestamp(timestamp)


def get_creation_date(image_path: Path) -> datetime:
    """
    Get creation date from EXIF metadata or file system.
    
    Tries EXIF first, falls back to file modification time.
    """
    exif_date = get_exif_datetime(image_path)
    if exif_date:
        return exif_date
    
    logging.warning(f"No EXIF date found for {image_path.name}, using file modification time")
    return get_file_datetime(image_path)


def get_target_folder(
    creation_date: datetime,
    target_base: Path,
    language: str
) -> Path:
    """
    Build target folder path based on creation date.
    
    Returns: target_base/YYYY/MM MonthName
    """
    year = creation_date.year
    month_num = creation_date.month
    month_name = MONTH_NAMES[language][month_num - 1]
    
    # Create folder name with zero-padded month number
    month_folder = f"{month_num:02d} {month_name}"
    
    return target_base / str(year) / month_folder


def is_image_file(filepath: Path) -> bool:
    """Check if file is a supported image format."""
    return filepath.suffix.lower() in SUPPORTED_EXTENSIONS


def shorten_filename(filename: str, max_length: int = 24) -> str:
    """Shorten long filenames for compact progress bar display."""
    if len(filename) <= max_length:
        return filename

    return f"{filename[: max_length - 3]}..."


def sort_photos(
    source: Path,
    target: Path,
    language: str = "en",
    dry_run: bool = False,
    verbose: bool = False
) -> None:
    """
    Sort photos from source to target based on creation date.
    
    Args:
        source: Source directory containing photos
        target: Target directory for sorted photos
        language: Month language ("en" or "de")
        dry_run: If True, only show planned operations
        verbose: If True, show detailed logging
    """
    logger = setup_logging(verbose)
    
    # Validate inputs
    if not source.is_dir():
        logger.error(f"Source directory not found: {source}")
        sys.exit(1)
    
    if language not in MONTH_NAMES:
        logger.error(f"Unsupported language: {language}. Choose 'en' or 'de'.")
        sys.exit(1)
    
    # Create target if it doesn't exist (unless dry-run)
    if not dry_run:
        target.mkdir(parents=True, exist_ok=True)
    
    # Find all image files
    image_files = [f for f in source.rglob("*") if f.is_file() and is_image_file(f)]
    
    if not image_files:
        logger.warning(f"No image files found in {source}")
        return
    
    logger.info(f"Found {len(image_files)} image(s) to process")
    
    if dry_run:
        logger.info("DRY RUN MODE: No files will be moved")
    
    # Process files with progress bar
    moved_count = 0
    failed_count = 0

    with tqdm(total=len(image_files), desc="Processing images", unit="file") as progress_bar:
        for image_file in image_files:
            progress_bar.set_postfix(
                file=shorten_filename(image_file.name),
                moved=moved_count,
                failed=failed_count,
                refresh=False,
            )

            try:
                # Get creation date
                creation_date = get_creation_date(image_file)

                # Build target folder
                target_folder = get_target_folder(creation_date, target, language)
                target_file = target_folder / image_file.name

                if dry_run:
                    logger.info(f"[DRY RUN] Would move: {image_file} -> {target_file}")
                else:
                    # Create target folder
                    target_folder.mkdir(parents=True, exist_ok=True)

                    # Handle file name conflicts
                    if target_file.exists():
                        base, ext = target_file.stem, target_file.suffix
                        counter = 1
                        while target_file.exists():
                            target_file = target_folder / f"{base}_{counter}{ext}"
                            counter += 1
                        logger.warning(f"File exists, renaming to: {target_file.name}")

                    # Move file
                    shutil.move(str(image_file), str(target_file))
                    logger.debug(f"Moved: {image_file.name} -> {target_file}")
                    moved_count += 1

            except Exception as e:
                logger.error(f"Failed to process {image_file.name}: {e}")
                failed_count += 1

            progress_bar.update(1)

        progress_bar.set_postfix(moved=moved_count, failed=failed_count, refresh=True)
    
    # Summary
    logger.info("=" * 60)
    if dry_run:
        logger.info(f"DRY RUN SUMMARY: Would have processed {len(image_files)} file(s)")
    else:
        logger.info(f"COMPLETED: {moved_count} file(s) moved, {failed_count} error(s)")
    logger.info("=" * 60)


def main():
    """Parse arguments and run the main function."""
    parser = argparse.ArgumentParser(
        description="Sort photos by creation date (EXIF metadata) into Year/Month folders.",
        epilog="Example: python sort_photos.py --source /path/to/photos --target /path/to/sorted"
    )
    
    parser.add_argument(
        "--source",
        type=Path,
        required=True,
        help="Path to the source folder containing photos"
    )
    
    parser.add_argument(
        "--target",
        type=Path,
        required=True,
        help="Path to the target folder for sorted photos"
    )
    
    parser.add_argument(
        "--language",
        type=str,
        default="en",
        choices=["en", "de"],
        help="Month language for folder names (default: en)"
    )
    
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show planned operations without moving files"
    )
    
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Enable detailed logging output"
    )
    
    argcomplete.autocomplete(parser)
    args = parser.parse_args()
    
    # Run sorting
    sort_photos(
        source=args.source,
        target=args.target,
        language=args.language,
        dry_run=args.dry_run,
        verbose=args.verbose
    )


if __name__ == "__main__":
    main()
