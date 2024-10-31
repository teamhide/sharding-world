import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("jacoco")
}

group = "com.teamhide"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.6.2")
    testImplementation("io.kotest:kotest-framework-datatest:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
    testImplementation("io.mockk:mockk:1.13.3")
    testImplementation("com.ninja-squad:springmockk:4.0.0")
    testImplementation("com.squareup.okhttp3:okhttp:4.11.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val testAll by tasks.registering {
    dependsOn("test", "jacocoTestReport", "jacocoTestCoverageVerification")
    tasks["test"].mustRunAfter(tasks["ktlintCheck"])
    tasks["jacocoTestReport"].mustRunAfter(tasks["test"])
    tasks["jacocoTestCoverageVerification"].mustRunAfter(tasks["jacocoTestReport"])
}

val snippetsDir by extra { file("build/generated-snippets") }

tasks.test {
    useJUnitPlatform()
    systemProperties["spring.profiles.active"] = "test"
    outputs.dir(snippetsDir)
    minHeapSize = "2048m"
    maxHeapSize = "2048m"
}

tasks.register("testUnit", Test::class) {
    useJUnitPlatform()
    systemProperties["spring.profiles.active"] = "test"
    exclude("**/*ControllerTest*")
}

tasks.register("teste2e", Test::class) {
    useJUnitPlatform()
    systemProperties["spring.profiles.active"] = "test"
    include("**/*ControllerTest*")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(false)
        csv.required.set(false)
    }
    finalizedBy(tasks.jacocoTestCoverageVerification)
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/*Application*",
                        "**/Q*Entity*",
                        "**/*logger*",
                        "**/*Logger*",
                        "**/**Logger**.class",
                        "**/**logger**.class",
                        "**logger*",
                    )
                }
            }
        )
    )
}

tasks.jacocoTestCoverageVerification {
    val queryDslClasses = ('A'..'Z').map { "*.Q$it*" }
    violationRules {
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "1.00".toBigDecimal()
            }
            classDirectories.setFrom(sourceSets.main.get().output.asFileTree)
            excludes = listOf(
                "com.teamhide.shardingworld.ShardingworldApplicationKt",
                "**/*logger*",
                "**/*Logger*",
                "**/**Logger**.class",
                "**/**logger**.class",
                "**logger*",
            ) + queryDslClasses
        }
    }
}
