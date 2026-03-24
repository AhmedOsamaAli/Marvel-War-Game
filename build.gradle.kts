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
        "--add-reads", "com.marvelwargame=ALL-UNNAMED",
        "-Dprism.vsync=false",
        "-Dprism.dirtyopts=false"
    )
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

// ── Native Windows app image (self-contained exe + bundled JRE) ──────────────
// Usage: .\gradlew.bat jpackageImage
// Output: build/jpackage/dist/MarvelWarGame/MarvelWarGame.exe
// Requires: JDK 17+ (jpackage is included). No WiX toolset needed.
tasks.register("jpackageImage") {
    group = "distribution"
    description = "Creates a self-contained Windows app image (.exe + bundled JRE) using jpackage."
    dependsOn("jar")

    doLast {
        val javaHome = System.getProperty("java.home")
        val distDir  = layout.buildDirectory.dir("jpackage/dist").get().asFile
        val inputDir = layout.buildDirectory.dir("jpackage/input").get().asFile

        distDir.deleteRecursively()
        inputDir.deleteRecursively()
        distDir.mkdirs()
        inputDir.mkdirs()

        // App JAR (modular — has module-info.class)
        val appJar = tasks.named<Jar>("jar").get().archiveFile.get().asFile

        // JavaFX JARs are modular → go on --module-path so jlink can process them
        // All other deps (vorbisspi etc.) are non-modular → go in --input (added to -cp by launcher)
        val allDeps       = configurations.named("runtimeClasspath").get()
                                .resolvedConfiguration.resolvedArtifacts
        val javafxJars    = allDeps.filter { it.moduleVersion.id.group == "org.openjfx" }.map { it.file }
        val classpathJars = allDeps.filter { it.moduleVersion.id.group != "org.openjfx" }.map { it.file }

        classpathJars.forEach { it.copyTo(inputDir.resolve(it.name), overwrite = true) }

        val modulePath = (javafxJars + appJar)
            .joinToString(File.pathSeparator) { it.absolutePath }

        exec {
            executable = "$javaHome/bin/jpackage"
            args(
                "--type",        "app-image",
                "--dest",        distDir.absolutePath,
                "--name",        "MarvelWarGame",
                "--app-version", project.version.toString(),
                "--description", "Marvel War Game — Tactical Champion Battles",
                "--module-path", modulePath,
                "--module",      "com.marvelwargame/com.marvelwargame.MarvelWarApp",
                "--input",       inputDir.absolutePath,
                "--java-options","--enable-native-access=javafx.graphics",
                "--java-options","--add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
                "--java-options","--add-reads=com.marvelwargame=ALL-UNNAMED"
            )
        }

        println("\n✅ App image ready: ${distDir.absolutePath}${File.separator}MarvelWarGame${File.separator}MarvelWarGame.exe")
    }
}
