plugins {
    war
}

group = "s21"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
    }
}

dependencies {
    implementation("org.springframework:spring-core:6.2.3")
    implementation("org.springframework:spring-context:6.2.3")
    implementation("org.springframework:spring-web:6.2.3")
    implementation("org.springframework:spring-webmvc:6.2.3")
    implementation("org.springframework.data:spring-data-jpa:3.4.3")

    implementation("org.springframework.security:spring-security-core:6.4.3")
    implementation("org.springframework.security:spring-security-config:6.4.3")
    implementation("org.springframework.security:spring-security-web:6.4.3")

    implementation("org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE")

    implementation("org.apache.commons:commons-dbcp2:2.13.0")
    implementation("org.hibernate.orm:hibernate-core:6.6.9.Final")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")


    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.7")
}

tasks.withType<War> {
    archiveFileName.set("ROOT.war")
}