# 📸 FaceFit AR
### Real-Time Augmented Reality Face Filter App for Android

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![ML Kit](https://img.shields.io/badge/ML%20Kit-4285F4?style=flat-square&logo=google&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-orange?style=flat-square)
![CameraX](https://img.shields.io/badge/CameraX-1.4.1-blue?style=flat-square)

---

## 📌 Overview

**FaceFit AR** is a native Android application that delivers an interactive augmented reality experience by applying digital face filters in real time. Using AI-based face detection and facial landmark tracking, filters automatically scale, rotate, and align with the user's face — responding dynamically to head movement, tilt, and distance from the camera.

> Built as part of an internship assessment project — demonstrating real-world Android development with AI, AR, and cloud integration.

---

## ✨ Features

- 🎭 **Real-Time AR Face Filters** — Filters rendered live on camera feed with zero post-capture processing
- 🤖 **AI Face Detection** — Google ML Kit detects facial landmarks (eyes, nose, ears) for accurate filter anchoring
- 🔐 **Firebase Authentication** — Secure Email/Password login & signup
- ☁️ **Cloud History** — Filter usage history stored in Firestore, private to each user
- 🖼️ **Gallery & Photos** — Capture, preview, delete and share filtered images
- 🔄 **Filter Carousel** — Swipeable horizontal filter selector during live camera preview
- 📷 **Front & Back Camera** — Flip lens support built in

---

## 🏗️ Architecture

FaceFit AR follows **MVVM (Model-View-ViewModel)** architecture with on-device AI processing:

```
CameraX (live frames)
        ↓
Google ML Kit (face detection + landmark extraction)
        ↓
Jetpack Compose Canvas (AR filter overlay rendering)
        ↓
Firebase (Authentication + Firestore history)
```

| Layer | Technology |
|---|---|
| View | Jetpack Compose (BOM 2024.11.00) |
| ViewModel | StateFlow + Coroutines |
| Model | FilterModel.kt + Firebase Repositories |

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Camera | CameraX 1.4.1 |
| Face Detection | Google ML Kit 16.1.7 |
| Authentication | Firebase Auth |
| Database | Firebase Firestore |
| Image Loading | Coil 2.2.2 |
| Navigation | Navigation-Compose |
| Firebase BoM | 33.10.0 |
| IDE | Android Studio Ladybug |

---

## 📱 Screens

| Screen | Description |
|---|---|
| Login / Signup | Branded auth screens with Firebase backend |
| AR Camera View | Full-screen live camera with filter overlay + flip lens |
| Filter Carousel | Swipeable LazyRow filter selector at bottom |
| Gallery & History | Dual-tab: local photos + cloud filter history |
| Image Preview | Minimalist screen with delete & share options |

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Ladybug or later
- Android device (real device recommended for ML Kit & CameraX testing)
- Firebase project set up

### Steps

1. **Clone the repository**
```bash
git clone https://github.com/VaishnaviSri16/FaceFitAR.git
cd FaceFitAR
```

2. **Firebase Setup**
   - Create a project on [Firebase Console](https://console.firebase.google.com/)
   - Enable **Authentication** (Email/Password) and **Firestore**
   - Download `google-services.json` and place it in the `app/` directory
   - Register your **SHA-1 fingerprint** in the Firebase Console

3. **Build & Run**
   - Open in Android Studio
   - Sync Gradle
   - Run on a physical Android device

---

## 🧩 Key Technical Challenges Solved

**1. Coordinate Mapping**
ML Kit landmarks use image buffer coordinates, not screen pixels. Solved by implementing a scaling matrix calculating the ratio between buffer and PreviewView dimensions.

**2. Performance Optimization**
Real-time AR processing was causing UI stutter. Solved by configuring `ImageAnalysis` with `STRATEGY_KEEP_ONLY_LATEST` to prevent frame backlog in the Compose UI.

**3. Firebase Sync Issues**
Firestore data wasn't syncing due to `google-services.json` plugin ordering. Solved by ensuring the Google services plugin was correctly applied in `app-level build.gradle`.

---

## 🔮 Future Improvements

- 🎯 Improved landmark detection for faster head motion tracking
- 🎨 Expanded filter library — seasonal, animated & custom overlays
- ☁️ Cross-device cloud image synchronization
- 🧪 Expanded unit test coverage for ViewModel logic

---

## 👩‍💻 Author

**Vaishnavi Srivastava**
B.Tech CSE 2026 | ITM Gida, Gorakhpur, Uttar Pradesh
Android Developer | Open to full-time opportunities

[![GitHub](https://img.shields.io/badge/GitHub-VaishnaviSri16-black?style=flat-square&logo=github)](https://github.com/VaishnaviSri16)

---

## 📜 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
