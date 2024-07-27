plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
    id("kotlin-parcelize")
    id("com.google.firebase.firebase-perf")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.compose.ui:ui:1.6.8")
    implementation ("androidx.compose.material:material:1.6.8")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.6.8")
    implementation ("androidx.activity:activity-compose:1.9.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-perf:21.0.1")
    implementation("com.google.firebase:firebase-crashlytics:19.0.3")
    implementation("com.google.firebase:firebase-analytics:22.0.2")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.6.8")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.6.8")

    // Compose dependencies
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation ("com.google.accompanist:accompanist-flowlayout:0.30.1")
    implementation ("com.google.accompanist:accompanist-navigation-animation:0.30.1")
    implementation ("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation ("com.google.maps.android:maps-compose:2.1.0")

    // Google Maps
    implementation ("com.google.android.gms:play-services-maps:19.0.0")

    // Coroutine Lifecycle Scopes
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")

    //Lifecycle
    implementation ("androidx.compose.runtime:runtime-livedata:1.6.8")

    // Paging Compose
    implementation ("com.google.accompanist:accompanist-pager:0.28.0")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.28.0")
    implementation ("androidx.paging:paging-compose:3.3.0")

    //Material Icon
    implementation ("androidx.compose.material:material-icons-extended:1.6.8")

    //Coil Image
    implementation ("io.coil-kt:coil-compose:2.4.0")

    //glide
    implementation("com.github.bumptech.glide:glide:4.14.2")

    //palette
    implementation ("androidx.palette:palette-ktx:1.0.0")

    //timber
    implementation ("com.jakewharton.timber:timber:5.0.1")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")

    //Icon
    implementation ("androidx.compose.material:material-icons-extended:1.6.8")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")

    //firebase
    implementation ("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation ("com.google.android.gms:play-services-auth:21.2.0")
    implementation (platform("com.google.firebase:firebase-bom:29.0.4"))

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation ("com.google.firebase:firebase-database")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation ("com.google.firebase:firebase-firestore-ktx:25.0.0")
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation ("com.google.firebase:firebase-storage-ktx:21.0.0")
    implementation("androidx.compose.foundation:foundation:1.6.8")
    implementation ("io.coil-kt:coil:2.4.0")// Ensure this is the correct version you are using
    implementation ("io.coil-kt:coil-compose:2.4.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0") // Ensure
    implementation ("androidx.work:work-runtime-ktx:2.9.0")
    implementation ("com.google.accompanist:accompanist-flowlayout:0.30.1")
    implementation ("androidx.datastore:datastore-preferences:1.1.1")
    implementation ("androidx.compose.material3:material3:1.2.1")


}

