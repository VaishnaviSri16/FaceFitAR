# 📸 FaceFit AR
### Real-Time Augmented Reality Face Filter App for Android

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![ML Kit](https://img.shields.io/badge/ML%20Kit-4285F4?style=flat-square&logo=google&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-orange?style=flat-square)
![CameraX](https://img.shields.io/badge/CameraX-1.4.1-blue?style=flat-square)

## 📌 Overview
FaceFit AR is a native Android application that delivers an interactive augmented reality experience by applying digital face filters in real time. Using AI-based face detection and facial landmark tracking, filters automatically scale, rotate, and align with the user's face — responding dynamically to head movement, tilt, and distance from the camera.

## ✨ Features
- 🎭 **Real-Time AR Face Filters** — Filters rendered live on camera feed
- 🤖 **AI Face Detection** — Google ML Kit detects facial landmarks for accurate filter anchoring
- 🔐 **Firebase Authentication** — Secure Email/Password login & signup
- ☁️ **Cloud History** — Filter usage history stored in Firestore, private to each user
- 🖼️ **Gallery & Photos** — Capture, preview, delete and share filtered images
- 🔄 **Filter Carousel** — Swipeable horizontal filter selector during live preview
- 📷 **Front & Back Camera** — Flip lens support built in

## 🏗️ Architecture
```
CameraX (live frames)
        ↓
Google ML Kit (face detection + landmark extraction)
        ↓
Jetpack Compose Canvas (AR filter overlay rendering)
        ↓
Firebase (Authentication + Firestore history)
```

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
| Firebase BoM | 33.10.0 |
| IDE | Android Studio Ladybug |

## ⚙️ Setup & Installation
1. Clone the repository
```bash
git clone https://github.com/VaishnaviSri16/FaceFitAR.git
cd FaceFitAR
```
2. Create a project on [Firebase Console](https://console.firebase.google.com/)
3. Enable Authentication (Email/Password) and Firestore
4. Download `google-services.json` and place it in the `app/` directory
5. Register your SHA-1 fingerprint in Firebase Console
6. Open in Android Studio → Sync Gradle → Run on physical device

## 🧩 Key Challenges Solved
- **Coordinate Mapping** — Implemented scaling matrix to map ML Kit landmarks to screen pixels
- **Performance Optimization** — Used `STRATEGY_KEEP_ONLY_LATEST` to prevent frame backlog
- **Firebase Sync** — Fixed `google-services.json` plugin ordering in `build.gradle`

## 🔮 Future Improvements
- Improved landmark detection for faster head motion
- Expanded filter library with animated overlays
- Cross-device cloud image synchronization

## 👩‍💻 Author
**Vaishnavi Srivastava**  
B.Tech CSE 2026 | ITM Gida, Gorakhpur  
Android Developer | Open to full-time opportunities

## 📜 License
This project is licensed under the MIT License.
