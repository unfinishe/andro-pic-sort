# Release Notes

## Version 1.1.0

**Release Date:** April 19, 2026

### What's New

#### Timestamp Preservation
- **Original file timestamps are now preserved** using OS-level copy operations by default.
- The app uses a two-strategy approach per file:
  - **Primary (OS copy)**: real on-disk paths are resolved for both source and target via the Linux `/proc/self/fd` symlink. `Files.copy(COPY_ATTRIBUTES)` is then used — the OS preserves mtime, atime and all attributes the filesystem supports automatically.
  - **Fallback (stream copy)**: if the storage provider does not expose a real file-system path (e.g. some OTG or virtual providers), the app falls back to stream copy with a subsequent `File.setLastModified()` call to at least restore the modification time.
- The report shows exactly how many files were processed with each strategy and, for stream-fallback files, whether modification time was successfully restored.

> **Note on file creation time:** The Linux kernel provides no syscall to write a file's birth/creation time (`btime`). This is a hard OS constraint — no Android app can work around it. **EXIF capture dates** (`DateTimeOriginal`, etc.) are unaffected: they live inside the file content and are copied verbatim by both strategies.

---

## Version 1.0.5 (Initial Release)

**Release Date:** April 14, 2026

### 🎉 What's New

#### Core Features
- **Easy Folder Selection**: Choose source and target folders via Android Storage Access Framework (SAF)
- **Flexible Organization**: Sort photos into `YYYY/MM MonthName` folder structure
- **Two Operation Modes**:
  - **Copy**: Safely duplicate files to target folder
  - **Move**: Relocate files to target folder
- **Smart Date Detection**:
  - Metadata date (EXIF) with automatic fallback to file creation date
  - File date only mode for faster processing
- **Conflict Resolution**: Choose between renaming (e.g., `photo_1.jpg`) or overwriting duplicates
- **Dry Run Mode**: Preview changes without modifying any files
- **Non-Image Support**: Optionally sort videos and other media (uses file date automatically)
- **Detailed Report**: See exactly what was processed, copied, moved, failed, or skipped

#### UI & Design
- Modern **Material 3** design conforming to Android Design Guidelines
- Adaptive layout for both portrait and landscape orientations
- Intuitive setup, progress, and results screens
- Clear section headers with helpful descriptions

#### Languages
- **English** (default)
- **German** (Deutsch) – automatically selected based on device locale

#### Performance & Safety
- **100% Offline**: All processing happens on your device, no network required
- **Privacy First**: No data collection, analytics, or tracking
- **Persistent Permissions**: Remembers your selected folders between app sessions
- **Automatic Fallback**: Gracefully handles missing EXIF data and metadata inconsistencies

### 📱 Supported Formats

#### Images
- JPEG, PNG, WebP, GIF, BMP, TIFF, HEIC, HEIF, DNG

#### Videos & Media
- MP4, MKV, MOV, WebM, and other Android-supported formats (when non-image sorting is enabled)

### 🔧 Technical Details

- **Target Platform**: Android 15 (API 35) and higher
- **Minimum Storage**: ~50 MB
- **Storage Access**: User-selected folder access via Android Storage Access Framework (SAF), including persisted URI access for future sessions
- **Architecture**: Kotlin + Jetpack Compose, Material 3
- **Build**: Release build with R8 code shrinking and resource optimization

### 🐛 Known Limitations

- **Performance**: Optimal performance for <10,000 files. Sorting very large libraries (>5,000 files) may take several minutes depending on device hardware and EXIF processing.
- **EXIF Consistency**: Some Android devices may not expose EXIF metadata consistently. The app automatically falls back to file creation date in these cases.
- **Storage**: Folder selection limited to device internal storage and readable external volumes via SAF.

### ✅ Quality Assurance

- Unit tests for core business logic
- Manual testing on Android 15 emulator and real devices
- Lint and static analysis checks

### 📖 Documentation

- **Privacy Policy**: https://github.com/unfinishe/andro-pic-sort/blob/main/PRIVACY_POLICY.md
- **Source Code**: https://github.com/unfinishe/andro-pic-sort
- **Issues & Feedback**: https://github.com/unfinishe/andro-pic-sort/issues
- **License**: MIT

### 🙏 Credits

Built with Kotlin, Jetpack Compose, and Material Design 3 for Android.

---

## Planned for Future Releases

- **Baseline Profiles** for improved startup and scroll performance
- **Additional Date Schemas** beyond the default `YYYY/MM MonthName`
- **Batch Job Scheduling** (optional)
- **Enhanced Error Handling** with detailed troubleshooting guides
- **Additional Language Support**

---

## How to Report Issues

Found a bug or have a feature request? Please open an issue on GitHub:
[https://github.com/unfinishe/andro-pic-sort/issues](https://github.com/unfinishe/andro-pic-sort/issues)

---

**Version:** 1.0.5  
**Build Date:** April 14, 2026  
**Repository:** https://github.com/unfinishe/andro-pic-sort  
**License:** MIT

