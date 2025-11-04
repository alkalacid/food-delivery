plugins {
    java
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "com.fooddelivery"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${rootProject.extra["springCloudVersion"]}"))

        // Lombok
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        // Spring Boot Starters
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("org.springframework.boot:spring-boot-starter-actuator")

        // Testing
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.mockito:mockito-core")
        testImplementation("org.mockito:mockito-junit-jupiter")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }
}

// Define common dependencies for specific service types
val webServices = listOf(
    "user-service",
    "restaurant-service",
    "order-service",
    "delivery-service",
    "payment-service",
    "notification-service",
    "api-gateway"
)

val kafkaServices = listOf(
    "user-service",
    "restaurant-service",
    "order-service",
    "delivery-service",
    "payment-service",
    "notification-service"
)

val databaseServices = listOf(
    "user-service",
    "restaurant-service",
    "order-service",
    "delivery-service",
    "payment-service",
    "notification-service"
)

configure(subprojects.filter { it.name in webServices }) {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
        
        // Resilience4j for Circuit Breaker, Retry, Bulkhead
        implementation("io.github.resilience4j:resilience4j-spring-boot3:2.1.0")
        implementation("org.springframework.boot:spring-boot-starter-aop")
        
        // OpenAPI/Swagger
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    }
}

configure(subprojects.filter { it.name in kafkaServices }) {
    dependencies {
        implementation("org.springframework.kafka:spring-kafka")
        testImplementation("org.springframework.kafka:spring-kafka-test")
    }
}

configure(subprojects.filter { it.name in databaseServices }) {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("com.microsoft.sqlserver:mssql-jdbc")
        
        // Liquibase for database migrations
        implementation("org.liquibase:liquibase-core")
        
        // Redis
        implementation("org.springframework.boot:spring-boot-starter-data-redis")
        implementation("redis.clients:jedis")
        
        // Testcontainers
        testImplementation("org.testcontainers:testcontainers")
        testImplementation("org.testcontainers:junit-jupiter")
        testImplementation("org.testcontainers:mssqlserver")
        testImplementation("org.testcontainers:kafka")
    }
}

extra["springCloudVersion"] = "2023.0.0"

tasks.register("cleanAll") {
    description = "Clean all subprojects"
    group = "build"
    dependsOn(subprojects.map { it.tasks.named("clean") })
}

tasks.register("buildAll") {
    description = "Build all subprojects"
    group = "build"
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.register("testAll") {
    description = "Test all subprojects"
    group = "verification"
    dependsOn(subprojects.map { it.tasks.named("test") })
}

