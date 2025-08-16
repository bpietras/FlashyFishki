# FlashLearn

[![Android](https://img.shields.io/badge/Platform-Android%2014-brightgreen.svg)](https://developer.android.com/about/versions/14)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.06.00-purple.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Table of Contents
- [Project Description](#project-description)
- [Tech Stack](#tech-stack)
- [Getting Started Locally](#getting-started-locally)
- [Available Scripts](#available-scripts)
- [Project Scope](#project-scope)
- [Project Status](#project-status)
- [License](#license)

## Project Description

FlashLearn is an Android mobile educational application that enables efficient learning through flashcards using spaced repetition methodology. The app allows users to create their own educational flashcards, categorize them, share with other users, and access flashcards shared by the community.

The main value proposition of the application is saving time when creating learning materials by leveraging community-created flashcards and efficient knowledge acquisition through the implemented 3-5-7 repetition algorithm.

The app is primarily targeted at students, teachers, and lifelong learners who want to effectively acquire and consolidate knowledge across various domains.

### Key Features
- User account system with email and password authentication
- Flashcard management (create, edit, delete, categorize)
- Spaced repetition system with 3-5-7 algorithm
- Community flashcard sharing and discovery
- Weekly learning progress reports and statistics

## Tech Stack

### Core Technologies
- **Kotlin 2.0.0** - Modern, concise programming language for Android
- **Jetpack Compose** - Declarative UI toolkit for building native Android UI
- **Navigation Compose** - Framework for implementing navigation between app screens
- **Hilt** - Dependency injection library for Android

### Database
- **SQLite** - Lightweight embedded database
- **Room** - ORM library providing an abstraction layer over SQLite

### Testing
- **AndroidX JUnit** - JUnit extension for Android
- **Espresso Core** - UI testing framework for Android

### CI/CD
- **GitHub Actions** - CI/CD pipeline platform

### Libraries & Dependencies
- AndroidX Core KTX (1.13.1)
- Lifecycle Runtime KTX (2.8.3)
- Activity Compose (1.9.1)
- Compose Material3
- Room (2.6.1)
- Kotlinx Serialization (1.6.3)
- Dagger Hilt (2.51.1)

## Getting Started Locally

### Prerequisites
- Android Studio Jellyfish or newer
- Android SDK 34 (Android 14)
- JDK 11 or higher

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/FlashLearn.git
   ```

2. Open the project in Android Studio.

3. Sync the project with Gradle files.

4. Build the project:
   ```bash
   ./gradlew build
   ```

5. Run the app on an emulator or physical device:
   ```bash
   ./gradlew installDebug
   ```

## Available Scripts

- `./gradlew build` - Build the project
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests
- `./gradlew installDebug` - Install the debug version on a connected device
- `./gradlew clean` - Clean the build directory

## Project Scope

### Included Features (MVP)
- User account system (registration, login, logout)
- Flashcard management (create, edit, delete, share)
- Flashcard categorization by domains
- 3-5-7 spaced repetition algorithm
- Public flashcard library with filtering and sorting
- Weekly learning progress reports
- Markdown export of flashcards

### Not in Scope (MVP)
- Advanced spaced repetition algorithms (like SuperMemo, Anki)
- Flashcard import from different formats (PDF, DOCX, etc.)
- Integration with other educational platforms
- Web application version (mobile-only for now)
- Flashcard quality verification mechanisms
- Gamification features
- Flashcard recommendation system
- Data loss protection mechanisms
- Inappropriate content reporting system

### Technical Constraints
- Android platform only (API 34, Android 14)
- Local database
- Offline functionality
- Limit of 1000 flashcards per user
- Text-only flashcards (no images, sounds, videos)

## Project Status

The project is currently in development. Version 1.0 is planned to include all MVP features.

### Performance Requirements
- Cold start: less than 5 seconds
- Warm start: less than 2 seconds
- Pagination implementation for performance optimization if needed

## License

This project is licensed under the MIT License - see the LICENSE file for details.
