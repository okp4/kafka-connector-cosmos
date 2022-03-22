import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application

    id("maven-publish")

    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.jmailen.kotlinter") version "3.9.0"
    id("io.gitlab.arturbosch.detekt") version "1.20.0-RC1"
}

group = "com.okp4"
version = "1.0-SNAPSHOT"
description = "A Kafka Connect CØSMOS connector for ingesting blocks from CØSMOS blockchains into Kafka."

repositories {
    mavenCentral()
}

dependencies {
    val kafkaVersion = "3.1.0"
    compileOnly("org.apache.kafka:connect-api:$kafkaVersion")
    compileOnly("org.apache.kafka:connect-runtime:$kafkaVersion")

    testImplementation(kotlin("test"))

    val kotestVersion = "5.2.1"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
}

tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
        archiveClassifier.set("standalone")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
            sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(fatJar)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.apply {
        jvmTarget = "11"
        allWarningsAsErrors = true
    }
}

application {
    mainClass.set("MainKt")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["fatJar"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/okp4/kafka-connector-cosmos")
            credentials {
                username = System.getenv("MAVEN_REPOSITORY_USERNAME")
                password = System.getenv("MAVEN_REPOSITORY_PASSWORD")
            }
        }
    }
}
