buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:8.0.2")  // replace with your Gradle version
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")  // replace with your Kotlin version
        //classpath ("com.google.dagger:hilt-android-gradle-plugin:2.45")
        classpath("com.google.gms:google-services:4.4.2")
        classpath ("com.google.firebase:firebase-crashlytics-gradle:2.7.1")
        classpath ("com.google.firebase:firebase-analytics:18.0.3")
        classpath("com.google.firebase:perf-plugin:1.4.2")
        classpath("com.google.firebase:firebase-appdistribution-gradle:5.0.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

