val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val mysql_version: String by project
val hikari_version: String by project
val jedis_version: String by project
val angus_mail_version: String by project
val dotenv_version: String by project
val nimbus_jwt_version: String by project

plugins {
    kotlin("jvm") version "2.3.20"
    id("io.ktor.plugin") version "3.4.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

group = "com.authorization"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("com.authorization.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("com.mysql:mysql-connector-j:$mysql_version")
    implementation("com.zaxxer:HikariCP:$hikari_version")
    implementation("redis.clients:jedis:$jedis_version")
    implementation("org.eclipse.angus:angus-mail:$angus_mail_version")
    implementation("aws.sdk.kotlin:ses:1.4.119")
    implementation("io.github.cdimascio:dotenv-kotlin:$dotenv_version")
    implementation("com.nimbusds:nimbus-jose-jwt:$nimbus_jwt_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    implementation("io.konform:konform:0.10.0")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks.test {
    environment("ENV_FILE", System.getenv("ENV_FILE") ?: ".env.testing.local")
}

// ktlint は Claude Code の PostToolUse フックから `./gradlew ktlintFormat` で
// 明示的に実行する運用。check/build への自動組み込み（ktlintCheck 系）は無効化し、
// CI の `gradle build` を汚さないようにする（Format 系タスクは有効なまま）。
tasks.matching { it.name.contains("Ktlint", ignoreCase = true) && it.name.contains("Check", ignoreCase = true) }
    .configureEach { enabled = false }
