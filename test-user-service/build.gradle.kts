plugins {
    id("java")
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.pragma.user"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring WebFlux (Reactive)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // R2DBC (Reactive Database)
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    
    // Redis Reactive
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    
    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    
    // Lombok (optional)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.test {
    useJUnitPlatform()
}
