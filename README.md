⭐ [Click here to watch the demo](https://youtu.be/Z-AHpPn4dGQ)

⭐ [Click here to download the Android App](https://1drv.ms/u/c/8557a44f0fe4c4ed/EfKLVJngxPtGi-KVtWlvo0QBhPk8TuDv46ajJI-yCQQ23A?e=uuLiAf)

⭐ [Click here to view the documentation](https://1drv.ms/b/c/8557a44f0fe4c4ed/EXTepreCNepPrzznxbxW098ByzMjyYJz44_AVohzWiSwcg?e=CemLq5)

⭐ [Click here to view the Presentation](https://1drv.ms/p/c/8557a44f0fe4c4ed/EXCXqkcObNxMlJ0h3fN1TZUBHQVbyLQx7lHAVSXZvFaqNg?e=SmMGBI)

----

# PROBLEM STATEMENT
Complex legal language acts as a significant barrier, preventing ordinary citizens from understanding their rights and engaging effectively with the justice system.


# Nyay Mitra - AI Legal Assistant ⚖️🤖

Nyay Mitra ("Friend of Justice") is an Android application designed to make legal information and assistance more accessible to the average person. Leveraging AI, it aims to simplify complex legal documents, answer legal queries in plain language, and provide basic guidance on user rights.

## 📖 Description

Understanding legal documents and navigating the legal landscape can be daunting. Nyay Mitra acts as a preliminary guide, using AI (powered by Google Gemini) to:

* **Simplify Legal Documents:** Scan or upload documents via image and get AI-powered explanations.
* **Answer Legal Questions:** Chat with an AI assistant trained on legal information.
* **Provide Information:** Access curated content on various legal topics through a dictionary format.
* **Stay Updated:** View a feed of recent legal news and updates.

**Note:** Nyay Mitra provides informational assistance and is **not** a substitute for professional legal advice from a qualified human lawyer.

---

## ✨ Features

* **User Authentication:**
    * Email/Password Sign-Up & Login (using Firebase Authentication).
    * Google Sign-In (using Firebase Authentication).
    * New user role selection flow (`SelectRoleActivity` - *Integration assumed*).
    * Backend integration for user registration/login and FCM token updates (via `HttpURLConnection`).
* **AI Assistant Tab:**
    * Real-time chat interface with Google Gemini (`gemini-2.5-flash`).
    * Document scanning integration using CameraX and ML Kit Text Recognition.
    * Ability to upload document images from the gallery (handled by `ScannerActivity` flow).
    * Chat history saved locally to device storage (JSON file).
    * Option to clear chat history.
* **Home Tab:**
    * Displays a scrolling list of legal news headlines (loaded from `strings.xml`).
    * Headlines feature a marquee effect if text is long.
    * Clickable items (currently show Toast, intended for links).
* **Dictionary Tab:**
    * Selection screen for "Lawyer" or "Client" focused content.
    * Displays formatted legal information (HTML within `strings.xml`) in dedicated activities (`LawyerContentActivity`, `ClientContentActivity`).
* **Chat (Laws) Tab:** Placeholder fragment for law-specific information.
* **Connect Tab:** Placeholder fragment for connecting with legal resources.
* **Profile Section (Accessible via Side Drawer):**
    * Displays user email and profile picture (synced across app).
    * **Update Profile:** Edit gender, age, academics (School, College, UG, PG, PhD), achievements. Includes profile picture upload from gallery with persistent storage access. Data saved using `SharedPreferences`.
    * **Settings:** Opens `SettingsActivity` with options for:
        * Language Selection (Spinner - Placeholder).
        * Privacy Policy (Opens URL).
        * Two-Factor Authentication (Switch - Placeholder).
        * Delete Account (Button with confirmation dialog - Placeholder).
    * **Theme Selection:** Dialog to switch between Light, Dark, and System Default themes (`AppCompatDelegate`).
    * **Notification Permissions:** Opens the app's system notification settings.
    * **Help Centre:** Opens `HelpActivity` displaying concise guides on app usage.
    * **FAQs:** Opens `FaqActivity` displaying a list of frequently asked questions and answers.
    * **Log Out:** Confirmation dialog and navigation back to `LoginActivity`, clearing user session (basic `SharedPreferences` clearing placeholder).
* **Notification System:**
    * Requests `POST_NOTIFICATIONS` permission on Android 13+.
    * Creates a Notification Channel.
    * Shows local notifications for:
        * AI/Scan response completion (if app is in background).
        * Daily Legal Tip (scheduled via `WorkManager`).
        * Manual test notification trigger.
    * Saves notification history using `SharedPreferences` (JSON via Gson).
    * **Notification History Screen:** Accessed via toolbar icon, displays saved notifications using a `RecyclerView`.
* **UI Enhancements:**
    * Custom Toolbar in `MainActivity` with centered, scrolling (marquee) title.
    * Material Design components (`BottomNavigationView`, `DrawerLayout`, `NavigationView`, `CardView`, `TextInputEditText`, `ShapeableImageView`, etc.).
    * Vector drawables for icons.
    * Gradient backgrounds for content pages.

---

## 🛠️ Technologies & Libraries Used

* **Language:** Java
* **Platform:** Android (Min SDK 24)
* **Architecture:** Activity/Fragment based (leaning towards separation of concerns)
* **Core Libraries:**
    * AndroidX (AppCompat, Core, Fragment, Activity, RecyclerView, ConstraintLayout)
    * Material Components for Android (UI elements)
* **AI & ML:**
    * Google AI SDK for Gemini (via `com.google.ai.client.generativeai:generativeai`)
    * ML Kit Text Recognition (`com.google.android.gms:play-services-mlkit-text-recognition`)
    * ML Kit Vision Common (`com.google.mlkit:vision-common`)
* **Camera:**
    * CameraX (`androidx.camera.*`)
* **Authentication:**
    * Firebase Authentication (Email/Password, Google Sign-In)
    * Google Sign-In SDK (`com.google.android.gms:play-services-auth`)
* **Background Tasks:**
    * Android WorkManager (`androidx.work:work-runtime`)
* **Networking:**
    * `java.net.HttpURLConnection` (for direct backend calls)
    * Retrofit & Gson (Included, but core AI uses Gemini SDK; used for Notification storage serialization)
* **Concurrency:**
    * `java.util.concurrent.ExecutorService`
    * `android.os.Handler` / `Looper`
    * `kotlinx-coroutines-android` (Dependency for Gemini SDK)
* **Data Persistence:**
    * `android.content.SharedPreferences` (for profile data, notification history, email)
* **Other:**
    * Guava (`com.google.guava:guava`) (Dependency for Gemini SDK)
    * Google Play Services Tasks (`com.google.android.gms:play-services-tasks`) (Dependency for ML Kit)

---

## 🚀 Setup & Installation

Follow these steps to set up and run the project:

1.  **Prerequisites:**
    * Android Studio (Latest stable version recommended)
    * JDK (Java Development Kit) 11 or 1.8 (check `compileOptions` in `build.gradle`)
    * Android SDK configured in Android Studio
    * An Android Emulator or Physical Device (running API 24 or higher)
    * A Firebase Project setup with Authentication (Email/Password and Google Sign-In) enabled.
    * A Google AI Studio API Key for Gemini.
    * (Optional) A running instance of the Flask backend if using the provided `HttpURLConnection` code.

2.  **Clone the Repository:**
    ```bash
    git clone https://github.com/Sachii257/hackathon-oct-2025-Code-Crew-
    ```

3.  **Import into Android Studio:**
    * Open Android Studio.
    * Select "Open" or "Import Project".
    * Navigate to the cloned directory and select it.
    * Android Studio will import the project and sync Gradle.

4.  **Add Configuration Files & API Keys (IMPORTANT - Do NOT commit these):**
    * **Firebase:** Download the `google-services.json` file from your Firebase project settings and place it in the `app/` directory of the project.
    * **Google Sign-In Web Client ID:** Find your Web Client ID in the Firebase Console (Authentication -> Sign-in method -> Google -> Web SDK configuration) or Google Cloud Console. Open `app/src/main/res/values/strings.xml` and add/replace the following string resource:
        ```xml
        <string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
        ```
    * **Gemini API Key:** Open `app/src/main/java/com/hackathon/nyaymitra/fragments/AiAssistantFragment.java`. Find the line:
        ```java
        private final String API_KEY = "YOUR_API_KEY_HERE";
        ```
        Replace `"YOUR_API_KEY_HERE"` with your actual Gemini API key.
        *(Note: For production, store API keys securely, e.g., in `local.properties` or using secrets management.)*
    * **(Optional) Backend URL:** Open `app/src/main/java/com/hackathon/nyaymitra/LoginActivity.java`. Find the line:
        ```java
        private static final String BACKEND_URL = "YOUR_BACKEND_NGROK_OR_IP_URL_HERE";
        ```
        Update this URL if you are running the corresponding backend server.

5.  **Sync Gradle:**
    * Android Studio should sync automatically after importing. If not, go to **File > Sync Project with Gradle Files**.

6.  **Build & Run:**
    * Select your target device (Emulator or Physical Device).
    * Ensure the `app` configuration is selected in the toolbar.
    * Click the "Run" button (green triangle).



---

## 🔮 Future Scope

* Implement Room Database for better data persistence.
* Refine AI prompts and context handling.
* Add more languages.
* Implement the "Connect" feature.
* Enhance document scanning capabilities.
* Complete placeholder features in Settings (2FA, actual Delete Account).

---



## 📄 License

MIT License

Copyright (c) 2025 Team Code Crew

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


---


## IMPACT
1. **Reduced Access to Justice**: This is the most direct and severe impact. When legal information is inaccessible, justice itself becomes inaccessible, particularly for marginalized and economically vulnerable populations.

2. **Increased Legal Costs**: The need for constant professional interpretation inflates legal expenses, making legal aid a luxury rather than a fundamental right.
3. **Delayed Resolution and Inefficiency**: Misunderstandings lead to procedural errors, incorrect filings, and prolonged legal battles, burdening both individuals and the judicial system.
4. **Erosion of Trust in the System**: When the law feels like a secret language, public trust in the transparency and fairness of the justice system diminishes.
5. **Disempowerment of Citizens**: Citizens cannot fully exercise their rights or fulfill their responsibilities if they cannot understand the laws that govern them. This hinders informed decision-making and civic participation.
6. **Amplification of Social Inequality**: Those with less education or financial means are disproportionately affected, widening the gap in legal empowerment.


---

## OTHER INFORMATION

- **Global Phenomenon**: While exacerbated in countries with diverse linguistic backgrounds and colonial legal legacies (like India), legalese is a challenge worldwide.

- **"Plain Language Movement"**: There's a global movement advocating for clearer, simpler legal writing in government documents, legislation, and judicial pronouncements. However, progress is slow due to ingrained practices and the perceived need for absolute precision.
- **Technological Potential**: AI and natural language processing (NLP) offer a promising avenue to bridge this gap, translating complex legal text into user-friendly explanations without requiring fundamental changes to existing legal documents.
- **Multilingual Challenge**: In countries like India, the problem is compounded by multiple official languages. Legal documents might be in English, but the local populace may primarily speak a regional language, necessitating translation and simplification.
- **The Goal of Nyay Mitra**: The Nyay Mitra app directly addresses this problem by leveraging AI to make legal information digestible and actionable for everyone, truly putting "a lawyer in your pocket" and striving to democratize access to legal understanding.