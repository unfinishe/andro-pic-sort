# Android Pic Sort

## Purpose
This project automatically sorts photos on an Android smartphone by creation date into year and month folders.

## Overview
The script reads date information from image metadata (EXIF) and moves files into a target structure following the pattern `Year/Month`.

Example structure:
```text
target_dir/
├── 2025/
│   ├── 01 January
│   ├── 02 February
│   └── ...
└── 2026/
    ├── 01 January
    └── ...
```

If a photo lacks EXIF date metadata, creation date from the file system is used as a fallback.

## Requirements
- Android smartphone
- Pydroid 3
- Python 3.10+
- Project dependencies are managed in `pyproject.toml`

## Quick Start (Android)
### 1. Set up Pydroid
1. Install Pydroid 3 from the Play Store.
2. Open the Pydroid terminal and install the project with its dependencies:

```bash
cd /path/to/android-pic-sort
pip install .
```

### 2. Set up folders
1. Source folder (unsorted photos), e.g.:
   `/storage/emulated/0/DCIM/Camera`
2. Target folder (sorted photos), e.g.:
   `/storage/emulated/0/Pictures/SortedPhotos`

Tip: Use internal storage when possible instead of SD card.

### 3. Grant permissions
1. Allow Pydroid access to files and media.
2. If needed, enable manually in Android settings:
   `Settings > Apps > Pydroid 3 > Permissions`

### 4. Run the script
```bash
python sort_photos.py \
	--source "/storage/emulated/0/DCIM/Camera" \
	--target "/storage/emulated/0/Pictures/SortedPhotos"
```

You can also use the installed console command:

```bash
android-pic-sort \
  --source "/storage/emulated/0/DCIM/Camera" \
  --target "/storage/emulated/0/Pictures/SortedPhotos"
```

## CLI Specification
### Required parameters
- `--source`: Path to the source folder with unsorted photos.
- `--target`: Path to the target folder for sorted photos.

### Optional parameters
- `--dry-run`: Show planned actions without moving files.
- `--verbose`: More detailed log output for diagnosis.
- `--language`: Month language (default: `en`, supported: `en` for English, `de` for German).
- `--help`: Show command-line help.

## Recommended safe workflow
1. First run with 10-20 test images.
2. Verify the output structure.
3. Then apply to the full camera folder.
4. Create a backup of important photos before production run.

## Common issues
- `PermissionError`: Check file/media access for Pydroid in settings.
- `No module named ...`: Install the project dependencies with `pip install .` or `pip install -e .`.
- Files not found: Verify source path under `/storage/emulated/0/...`.
- Slow processing: Work in smaller batches and keep display active during execution.
- Missing creation date: Some images may not have EXIF date metadata.

## Examples
Test run without moving files:
```bash
python sort_photos.py \
  --source "/storage/emulated/0/Download/TestPhotos" \
  --target "/storage/emulated/0/Download/SortedTestPhotos" \
  --dry-run
```

Production run with German month names:
```bash
python sort_photos.py \
  --source "/storage/emulated/0/DCIM/Camera" \
  --target "/storage/emulated/0/Pictures/SortedPhotos" \
  --language de
```

## Development

### Set up local Python environment
Clone or navigate to the project directory and create a virtual environment:

```bash
# Create virtual environment
python3 -m venv venv

# Activate it
# On macOS/Linux:
source venv/bin/activate
# On Windows:
venv\Scripts\activate
```

### Install dependencies
```bash
pip install --upgrade pip
pip install -e .
```

This installs the project in editable mode and pulls dependencies from `pyproject.toml`.

### Run the script in development
Test the script on your desktop with local folders:

```bash
# Dry run (recommended first)
python sort_photos.py \
  --source ~/Downloads/TestPhotos \
  --target ~/Downloads/SortedPhotos \
  --dry-run --verbose

# Actual run
python sort_photos.py \
  --source ~/Downloads/TestPhotos \
  --target ~/Downloads/SortedPhotos
```

### Module structure
The project is organized as follows:
```
src/
├── __init__.py           # Package metadata
├── __main__.py           # Entry point for `python -m src`
└── sort_photos.py        # Main implementation
sort_photos.py            # Root wrapper script
```

### Running the module directly
You can also run it as a Python module:

```bash
python -m src --source ~/Downloads/TestPhotos --target ~/Downloads/SortedPhotos --dry-run
```

### Alternative development commands
You can use any of these entry points during development:

```bash
python sort_photos.py --source ~/Downloads/TestPhotos --target ~/Downloads/SortedPhotos --dry-run
python -m src --source ~/Downloads/TestPhotos --target ~/Downloads/SortedPhotos --dry-run
android-pic-sort --source ~/Downloads/TestPhotos --target ~/Downloads/SortedPhotos --dry-run
```

### Create test photos
To test locally without real photos, use sample images from unsplash or create dummy files:

```bash
# Create test directory structure
mkdir -p ~/Downloads/TestPhotos

# Copy some images or create dummy JPEG files
cp ~/Pictures/*.jpg ~/Downloads/TestPhotos/
```

### Deactivate environment
When done developing:

```bash
deactivate
```

### Shell Completion (Tab Completion)

For bash, zsh, or similar shells, enable tab completion for the `android-pic-sort` command:

```bash
# Register completion (one-time setup after pip install -e .)
eval "$(register-python-argcomplete android-pic-sort)"
```

After that, you can press Tab to complete flags:
```bash
android-pic-sort --s<TAB>      # completes to --source
android-pic-sort --t<TAB>      # completes to --target
android-pic-sort --l<TAB>      # completes to --language
```

To make it persistent, add the above line to your shell config (`.bashrc`, `.zshrc`, etc.):

```bash
echo 'eval "$(register-python-argcomplete android-pic-sort)"' >> ~/.zshrc
source ~/.zshrc
```

## License
This project is licensed under the MIT License.