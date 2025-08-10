# Pprominec

An Android application for geodesic calculations and mapping.

[Report Bug](https://github.com/onelenyk/pprominec/issues) · [Request Feature](https://github.com/onelenyk/pprominec/issues)


## Table of Contents

- [About The Project](#about-the-project)
  - [Key Features](#key-features)
  - [Built With](#built-with)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)
- [Acknowledgments](#acknowledgments)


## About The Project

Pprominec is an Android application for geodesic calculations and mapping. It is designed for users who need to perform surveying, navigation, or other tasks that require precise geographic calculations. The name "Pprominec" is derived from the Ukrainian word "промінець" (prominets), which means "little ray" or "beam," reflecting the app's focus on azimuths and lines of sight.

### Key Features

*   **Geodesic Calculations:** Perform direct and inverse geodesic calculations to determine target coordinates, azimuths, and distances.
*   **Interactive Map:** Visualize geographic data and interact with an offline-capable map.
*   **User Markers:** Add, update, and delete custom markers on the map to mark points of interest.
*   **Location Services:** Use the device's location to center the map or as a starting point for calculations.
*   **Offline Maps:** Support for offline maps, making it suitable for fieldwork in areas with limited connectivity.
*   **User-Friendly Input:** Includes utilities for parsing and normalizing user input for coordinates, azimuths, and distances.

### Built With

This project is built with a modern Android tech stack:

*   [Kotlin](https://kotlinlang.org/)
*   [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   [Decompose](https://github.com/arkivanov/Decompose)
*   [Koin](https://insert-koin.io/)
*   [osmdroid](https://github.com/osmdroid/osmdroid)
*   [mapsforge](https://github.com/mapsforge/mapsforge)
*   [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)


## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

You need Android Studio to build and run the project.
*   [Android Studio](https://developer.android.com/studio)

### Installation

1.  Clone the repo
    ```sh
    git clone https://github.com/onelenyk/pprominec.git
    ```
2.  Open the project in Android Studio.
3.  Let Android Studio sync the project and download dependencies.
4.  Run the app on an emulator or a physical device.

## Usage

Once the app is running, you can use it to:
*   Enter coordinates for two points (A and B) to calculate the azimuth and distance between them.
*   Enter a starting point (A), an azimuth, and a distance to find a target point.
*   Add markers to the map by long-pressing on a location.
*   View your current location on the map.

## Roadmap

- [ ] File Import/Export (KML, GPX, CSV)
- [ ] Improved UI/UX
- [ ] Advanced Geodesic Tools (e.g., coordinate transformations)
- [ ] Expand shared Kotlin Multiplatform module
- [ ] Cloud Sync for user data
- [ ] Comprehensive Testing
- [ ] CI/CD Pipeline

See the [open issues](https://github.com/onelenyk/pprominec/issues) for a full list of proposed features (and known issues).

## Acknowledgments

*   [GeographicLib](https://geographiclib.sourceforge.io/)
*   [osmdroid](https://github.com/osmdroid/osmdroid)
*   [mapsforge](https://github.com/mapsforge/mapsforge)
*   [Best-README-Template](https://github.com/othneildrew/Best-README-Template)
