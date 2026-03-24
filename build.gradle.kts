plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.marvelwargame"
version = "2.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.media")
}

dependencies {
    // OGG/Vorbis decoder for javax.sound.sampled (Windows OGG support)
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainModule = "com.marvelwargame"
    mainClass  = "com.marvelwargame.MarvelWarApp"
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=javafx.graphics",
        "--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
        "--add-reads", "com.marvelwargame=ALL-UNNAMED"
    )
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}
