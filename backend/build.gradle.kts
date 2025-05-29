plugins {
    java
    id("org.springframework.boot") version "2.7.12"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
}

group = "com.pulmocare"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.data:spring-data-mongodb")
    implementation(libs.google.cloud.aiplatform)
    implementation(libs.google.auth.library.oauth2.http)
    implementation(libs.google.cloud.vertexai)
    implementation("com.google.auth:google-auth-library-oauth2-http:1.35.0")
    implementation("com.microsoft.onnxruntime:onnxruntime:1.15.1")
    implementation("org.bytedeco:opencv-platform:4.10.0-1.5.11")
    implementation("io.github.cdimascio:dotenv-java:2.3.2") // For loading .env files
    // 2. Google Vertex AI client
    implementation("com.google.cloud:google-cloud-vertexai:1.23.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
