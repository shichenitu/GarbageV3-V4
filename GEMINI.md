# Garbage Sorting Copenhagen (GarbageV1)

A comprehensive Android application built with Kotlin and Jetpack Compose to assist residents of Copenhagen with garbage sorting and recycling.

## Project Overview

The app helps users identify the correct disposal methods for various items, track their recycling habits, and receive timely reminders via geofencing when they are near recycling stations.

### Key Technologies

- **UI Framework:** Jetpack Compose with Material 3.
- **Architecture:** Clean Architecture / Domain-Driven Design (DDD).
- **Dependency Injection:** Hilt.
- **Navigation:** Jetpack Compose Navigation (Type-safe).
- **Backend/Database:** Firebase Firestore (Bins, Items).
- **Networking:** Retrofit (integrates with Copenhagen Open Data for recycling stations).
- **Location & Background:** 
    - Google Play Services Geofencing.
    - WorkManager for intelligent geofence-triggered background tasks.
- **Persistence:** Jetpack DataStore for user preferences.

## Project Structure

- `dk.chen.garbagev1.core`: Core system integration including `GeofenceManager`, `NotificationHelper`, and background `workers`.
- `dk.chen.garbagev1.data`: Implementation of repositories, data models (DTOs), and remote service integrations (Firebase, Retrofit).
- `dk.chen.garbagev1.domain`: Business logic, repository interfaces, and pure domain models.
- `dk.chen.garbagev1.ui`: The presentation layer using Jetpack Compose.
    - `components`: Reusable UI widgets.
    - `features`: Feature-based screen implementations (Garbage sorting, Recycling, Settings).
    - `navigation`: Type-safe navigation graph and bottom bar setup.
    - `theme`: App-wide styling (colors, typography, theme).

## Features

- **Garbage Search:** Search for items to find the correct bin (e.g., "Plastic", "Metal").
- **Recycling Tracking:** Record when you last emptied specific bins to maintain a recycling schedule.
- **Smart Reminders:** Automatically notifies you when you are near a recycling station if any of your bins are "overdue" (older than 7 days).
- **Affald CPH Integration:** Quick access to official sorting information for Copenhagen.

## Building and Running

### Prerequisites
- Android Studio Ladybug or newer.
- JDK 11+.
- A valid `google-services.json` file in the `app/` directory (required for Firebase).

### Commands
- **Build APK:** `./gradlew assembleDebug`
- **Run Unit Tests:** `./gradlew test`
- **Run UI Tests:** `./gradlew connectedAndroidTest`
- **Install on Device:** `./gradlew installDebug`

## Development Conventions

- **State Management:** Uses ViewModels with `StateFlow` and `SharedFlow` for UI events.
- **Concurrency:** Kotlin Coroutines and Flow are used throughout the data and domain layers.
- **Type Safety:** Navigation routes are defined using Kotlin Serialization classes.
- **UI:** Strictly follows Jetpack Compose best practices with a focus on unidirectional data flow.

## Resources
- **Copenhagen Open Data API:** Used for fetching real-time recycling station locations.
- **Icons:** Material Icons Extended.

