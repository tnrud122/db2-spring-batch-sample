plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // MyBatis (Spring Boot 4 compatible)
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.0")
    // MyBatis-Spring provides Spring Batch item reader/writer integrations
    implementation("org.mybatis:mybatis-spring:4.0.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // DB2 JDBC driver
    runtimeOnly("com.ibm.db2:jcc:12.1.3.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
