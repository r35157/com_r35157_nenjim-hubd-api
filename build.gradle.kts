import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.registering

plugins {
    id("java")
    id("maven-publish")
}

group = "com.r35157.nenjim"
version = project.findProperty("version") ?: "UNSET"
if (version == "UNSET" && gradle.startParameter.taskNames.any { it.startsWith("publish") }) {
    throw GradleException("You must set -Pversion=... (use publish.sh / publishCICD.sh)")
}

repositories {
    mavenLocal()

    maven {
        name = "Artifacts"
        url = uri("/mnt/artifacts/java")

        // Failsafe for Android variant-aware deps + klassiske Maven-artefakter:
        metadataSources {
            gradleMetadata()  // bevar .module for Android/AAR varianter
            mavenPom()
            artifact()
        }
    }

    mavenCentral()
}

val detag by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    detag("com.r35157.tools:detag-impl_ref:0.1.0")
    compileOnly("org.jetbrains:annotations:26.1.0")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

val generatedDetagMain = layout.buildDirectory.dir("generated/sources/detag/main/java")

val cleanGeneratedDetagMain by tasks.registering(Delete::class) {
    delete(generatedDetagMain)
}

val detagMain by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Generates Java sources from .tjava files"

    classpath = detag
    mainClass.set("com.r35157.tools.detag.impl.ref.Main")

    val configFile = layout.projectDirectory.file("../detag.conf")
    val sourceRoot = layout.projectDirectory.dir("src/main/tjava")

    inputs.file(configFile)
    inputs.dir(sourceRoot)
    outputs.dir(generatedDetagMain)

    dependsOn(cleanGeneratedDetagMain)

    args(
        "--config", configFile.asFile.absolutePath,
        "--source-root", sourceRoot.asFile.absolutePath,
        "--out", generatedDetagMain.get().asFile.absolutePath
    )
}

sourceSets {
    main {
        // Human-written Detag source files. IntelliJ should treat this as a source root.
        // Gradle's Java compiler will still only compile .java files directly from sourceSets,
        // so the .tjava files are not compiled directly.
        java.srcDir("src/main/tjava")
    }
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(detagMain)

    // Compiler input generated from src/main/tjava.
    // Do not add this directory to sourceSets, or IntelliJ will see duplicate classes:
    // MyClass.tjava + build/generated/.../MyClass.java.
    source(generatedDetagMain)
}

val libsDir = layout.buildDirectory.dir("libs")

tasks.register<Sync>("prepareLibs") {
    group = "distribution"
    description = "Copies runtime deps to build/libs without deleting the app jar"

    val jarTask = tasks.named<Jar>("jar")
    dependsOn(jarTask)

    into(libsDir)

    // Kun deps (transitivt)
    from(configurations.runtimeClasspath)

    // Bevar jar-filen som jar-tasken allerede har lagt i build/libs
    preserve {
        include(jarTask.get().archiveFileName.get())
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "Artifacts"
            url = uri("/mnt/artifacts/java")
        }
    }
}
