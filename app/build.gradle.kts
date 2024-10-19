plugins {
    application
    jacoco
    checkstyle
    id("java")
    id("io.freefair.lombok") version "8.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

application {
    mainClass = "hexlet.code.App"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.11")
    implementation("io.javalin:javalin-bundle:6.3.0")
    implementation("gg.jte:jte:3.1.13")
    implementation("io.javalin:javalin-rendering:6.3.0")
    implementation("org.projectlombok:lombok:1.18.30")
    compileOnly ("org.projectlombok:lombok:1.18.34")
    annotationProcessor ("org.projectlombok:lombok:1.18.34")
    implementation("com.zaxxer:HikariCP:6.0.0")
    implementation("com.h2database:h2:2.2.220")
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
    implementation("com.konghq:unirest-java-core:4.2.0")
    implementation("com.konghq:unirest-java-bom:4.2.0")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.postgresql:postgresql:42.6.0")
}

tasks.test {
    useJUnitPlatform()
}
tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}