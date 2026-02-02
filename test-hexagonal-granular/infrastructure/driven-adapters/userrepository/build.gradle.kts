plugins {
    id("java-library")
}

description = "Driven adapter - redis"

dependencies {
    // Domain dependencies
    implementation(project(":domain:model"))
    implementation(project(":domain:ports"))

    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // Redis dependencies
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
