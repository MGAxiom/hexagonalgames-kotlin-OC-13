plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
  alias(libs.plugins.googleGmsGoogleServices)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "com.openclassrooms.hexagonal.games"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.openclassrooms.hexagonal.games"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.11"
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
  }
}

dependencies {
  //kotlin
  implementation(platform(libs.kotlin.bom))

  //DI
  implementation(libs.hilt)
  implementation(libs.firebase.firestore)

  ksp(libs.hilt.compiler)
  implementation(libs.hilt.navigation.compose)

  //compose
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.graphics)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.material)
  implementation(libs.compose.material3)
  implementation(libs.lifecycle.runtime.compose)
  debugImplementation(libs.compose.ui.tooling)
  debugImplementation(libs.compose.ui.test.manifest)

  implementation(libs.activity.compose)
  implementation(libs.navigation.compose)
  
  implementation(libs.kotlinx.coroutines.android)
  
  implementation(libs.coil.compose)
  implementation(libs.accompanist.permissions)

  //Firebase BoM
  implementation(platform(libs.firebase.bom))

  //Firebase
  implementation(libs.google.firebase.auth)
  implementation(libs.firebase.ui.auth)
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.messaging)
  implementation(libs.firebase.storage)

  testImplementation(libs.junit)
  androidTestImplementation(libs.ext.junit)
  androidTestImplementation(libs.espresso.core)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.core.testing)
}
