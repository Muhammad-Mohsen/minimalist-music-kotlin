plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
}

android {
	namespace = "com.minimalist.music"
	compileSdk = 35

	defaultConfig {
		applicationId = "mohsen.muhammad.minimalist"
		minSdk = 27
		targetSdk = 35
		versionCode = 28
		versionName = "5.0"
	}

	compileOptions {
		sourceCompatibility(JavaVersion.VERSION_21)
		targetCompatibility(JavaVersion.VERSION_21)
	}

	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_21.toString()
	}

	kotlin {
		jvmToolchain(21)
	}

	bundle {
		storeArchive {
			enable = false
		}
	}

	buildFeatures {
		buildConfig = true
	}

	buildTypes {
		debug {
			isMinifyEnabled = false
			isDebuggable = true
			applicationIdSuffix = ".debug"
		}
		release {
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

			ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
		}
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)

	implementation(libs.androidx.media)

	// metadata
	implementation(libs.ffmpegmediametadataretriever.core)
	implementation(libs.ffmpegmediametadataretriever.native)
}