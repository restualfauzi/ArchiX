# ArchiX - Manage Files. Extract Anything.

ArchiX is a modern, lightweight, and fully functional local Android file manager and archive utility. It allows users to browse internal storage, manage files and folders, and seamlessly create or extract various archive formats including ZIP, RAR, 7Z, TAR, and GZ directly on their mobile device.

Built entirely using modern Android development practices, ArchiX focuses on providing a clean, iOS-inspired user interface while maintaining native Android performance and respecting user privacy by executing all operations completely offline.

## Key Features

*   **Modern User Interface:** Built natively with Jetpack Compose featuring an edge-to-edge layout, glassmorphism bottom navigation, and a clean, flat design language inspired by iOS.
*   **Comprehensive File Management:** Full support for exploring directories, creating folders, renaming, moving (cut), copying, sharing, and deleting files.
*   **Advanced Archive Support:** 
    *   Extract multiple formats: ZIP, RAR, 7Z, TAR, and GZ.
    *   Create ZIP archives with real-time compression progress.
    *   Peek into archive contents without extracting them first.
    *   Built-in Zip Slip vulnerability protection for safe extraction.
*   **Instant Search:** Rapid file and folder search by name or extension with category filtering.
*   **Storage Insights:** Visual representation of internal storage usage with dual-ring progress indicators and automatic file categorization (Images, Videos, Audio, Documents, APKs).
*   **Quick Access:** Dedicated views for Recent Files, Large Files (over 50MB), and User Favorites.
*   **Customization:** Fully supports System Dark/Light themes, multiple view modes (List and Grid), and custom sorting options (Name, Size, Date, Type).
*   **Privacy First:** 100% offline. No analytics, no cloud uploads, and no backend servers.

## Tech Stack & Architecture

ArchiX is developed using the latest recommended Android tech stack:

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** Model-View-ViewModel (MVVM)
*   **Asynchrony:** Kotlin Coroutines & Flow
*   **Local Storage:** Jetpack DataStore Preferences
*   **Navigation:** Jetpack Navigation Compose
*   **File System:** Android Storage Access Framework (SAF) and `java.io`
*   **Archive Libraries:**
    *   `java.util.zip` (Native ZIP compression/extraction)
    *   `org.apache.commons:commons-compress` (TAR, GZ, 7Z parsing)
    *   `org.tukaani:xz` (Required by Commons Compress for 7Z decompression)
    *   `com.github.junrar:junrar` (RAR extraction)

## Requirements

*   Minimum SDK: Android 6.0 (API Level 23)
*   Target SDK: Android 15 (API Level 35+)
*   Android Studio: Ladybug Feature Drop (2024.2.2) or newer
*   Gradle Plugin: 8.7+

## Building and Running

1.  Clone the repository to your local machine:
    ```bash
    git clone https://github.com/yourusername/ArchiX.git
    ```
2.  Open the project in Android Studio.
3.  Allow Gradle to sync the dependencies.
4.  Connect an Android device or start an emulator.
5.  Click the "Run" button in Android Studio or use the Gradle wrapper from the terminal:
    ```bash
    ./gradlew assembleDebug
    ```

## Permissions Explained

ArchiX requests the following permissions to function correctly:

*   `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`: Required for Android 10 and below to read and modify files in the shared storage.
*   `MANAGE_EXTERNAL_STORAGE`: Required for Android 11 and above (Scoped Storage) to allow the app to act as a fully-featured file manager and access directories outside its app-specific sandbox.
*   `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`: Media-specific permissions for modern Android versions to accurately calculate category sizes and display recent media files.

The app provides an in-app banner explaining why these permissions are needed before prompting the system dialog.

## Project Structure

The codebase is organized by feature and layer to ensure scalability:

*   `data/local`: Contains DataStore implementations for user preferences.
*   `data/model`: Kotlin data classes and enums representing files, archives, and UI states.
*   `data/repository`: Business logic for file operations (`FileRepository`) and compression/decompression (`ArchiveRepository`).
*   `ui/components`: Reusable Jetpack Compose UI elements (Cards, Dialogs, List Items).
*   `ui/screens`: Main application screens (Home, FileManager, Archive, Search, More, Settings, etc).
*   `ui/theme`: Compose color schemes, typography, and theme definitions.
*   `ui/viewmodel`: AndroidX ViewModels managing UI state and interacting with repositories.
*   `utils`: Helper objects for formatting file sizes, dates, and storage calculations.
*   `navigation`: Jetpack Navigation graph configuration and route definitions.

## Known Limitations

*   RAR extraction relies on `junrar`, which only supports RAR4 format natively. RAR5 archives may fail to extract depending on their compression dictionary.
*   Extracting extremely large 7Z files requires sufficient available device RAM, as the current implementation uses a streaming buffer that may spike under heavy LZMA loads.
*   Creating archives other than ZIP is currently not supported.

## Contributing

Contributions are welcome. If you find a bug or have a feature request, please open an issue first. For code contributions:

1.  Fork the repository.
2.  Create a feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

Please ensure your code follows standard Kotlin style guides and does not introduce regressions to the core file manipulation logic.

## License

Distributed under the MIT License. See `LICENSE` for more information.
