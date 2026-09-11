import org.jooq.meta.jaxb.Property

plugins {
    java
    application
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("nu.studer.jooq") version "10.2.1"
}

group = "com.authorization"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.authorization.Application")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    implementation("org.jooq:jooq:3.21.5")
    implementation("com.mysql:mysql-connector-j:9.1.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.2.0")
    implementation("com.nimbusds:nimbus-jose-jwt:9.40")
    implementation("io.github.cdimascio:dotenv-java:3.0.2")

    implementation(platform("software.amazon.awssdk:bom:2.29.1"))
    implementation("software.amazon.awssdk:ses")

    jooqGenerator("org.jooq:jooq-meta-extensions:3.21.5")
    jooqGenerator("org.jooq:jooq-codegen:3.21.5")
    jooqGenerator("org.jooq:jooq-meta:3.21.5")
    jooqGenerator("org.jooq:jooq:3.21.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

jooq {
    version.set("3.21.5")
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                generator.apply {
                    name = "org.jooq.codegen.JavaGenerator"
                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        properties.add(Property().withKey("scripts").withValue("src/main/resources/db/schema.sql"))
                        properties.add(Property().withKey("sort").withValue("semantic"))
                        properties.add(Property().withKey("defaultNameCase").withValue("lower"))
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = false
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.authorization.jooq"
                        directory = "build/generated-src/jooq/main"
                    }
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    environment("ENV_FILE", System.getenv("ENV_FILE") ?: ".env.testing.local")
}
