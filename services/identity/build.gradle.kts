plugins {
  id("java")
  id("org.springframework.boot") version "2.6.2"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("com.diffplug.spotless") version "5.9.0"
}

repositories {
    mavenCentral()
}
group = "com.crapi"
version = "2.3.0.RELEASE"
java.sourceCompatibility = JavaVersion.VERSION_11

pluginManager.withPlugin("java") {
    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            removeUnusedImports()
            googleJavaFormat("1.7")
            indentWithSpaces(4)
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
dependencies {
    val lombokVersion = "1.18.12"
    val mockito = "3.7.7"
    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("javax.annotation:javax.annotation-api:1.3.2")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.postgresql:postgresql:runtime")
    implementation("org.postgresql:postgresql:42.4.0")
    implementation("com.google.cloud:google-cloud-storage:2.10.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
    implementation("com.google.cloud:libraries-bom:25.4.0")
    implementation("com.google.cloud:google-cloud-storage:2.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:${mockito}")
    testImplementation("org.mockito:mockito-core:${mockito}")
    testImplementation("org.mockito:mockito-inline:${mockito}")
    testImplementation("junit:junit:4.13.1")
}
