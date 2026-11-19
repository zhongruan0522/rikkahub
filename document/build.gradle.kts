plugins {
    id("rikkahub.android.library")
}

android {
    namespace = "me.rerere.document"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
