# How to get the MeChat debug APK (no technical skills needed)

1. Open your repository on **github.com** in a browser and make sure you are logged in.
2. Click the **“Actions”** tab near the top of the page.
3. In the left list, click **“android”**, then click the **latest run** (the newest entry at the top).
4. Scroll to the bottom of the run page to the **“Artifacts”** section.
5. Click **“mechat-debug-apk”** — a zip will download. Unzip it; inside is `app-debug.apk`.
6. Send `app-debug.apk` to your Android phone (USB, cloud drive, or chat) and tap it to install.
   - If Android warns about unknown sources, allow installation for the app you used to open it.
7. (Optional) The same page also has **“gradle-wrapper-files”** — download it if you want to
   commit `gradlew` / `gradle/wrapper/` to the repo later.

The APK is rebuilt automatically on every push. Debug builds are not signed for release —
use a tagged release (`v*`) for the signed APK.
