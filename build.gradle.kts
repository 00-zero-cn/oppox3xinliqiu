plugins { id("com.android.application") }

android {
    namespace = "com.example.watchrecorder"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.watchrecorder"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
}

dependencies {}
