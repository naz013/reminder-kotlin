plugins {
  id("reminder.kotlin.jvm")
}

dependencies {
  testImplementation(libs.junit)
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
}
