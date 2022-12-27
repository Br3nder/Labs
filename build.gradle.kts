import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation ("org.apache.commons:commons-csv:1.5")
    implementation("dev.inmo:tgbotapi:4.2.2")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.microsoft.sqlserver:mssql-jdbc:11.2.1.jre17")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}