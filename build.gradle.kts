import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    val kotlin_version = "1.6.10"
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlin_version")
}
