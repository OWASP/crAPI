plugins {
  id("java")
  application
  id("org.springframework.boot") version "2.6.1"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("com.diffplug.spotless") version "5.9.0"
}

group = "com.crapi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClassName = "com.crapi.CRAPIBootApplication"
}

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
    val springBootVersion = "2.6.1"
    val log4jVersion = "2.14.0"
    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("javax.annotation:javax.annotation-api:1.3.2")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    implementation("org.springframework.boot:spring-boot-starter:${springBootVersion}") 
    implementation("org.springframework.boot:spring-boot-starter-web:${springBootVersion}") 
    implementation("org.springframework.boot:spring-boot-starter-security:${springBootVersion}") 
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:${springBootVersion}") 
    implementation("org.springframework.boot:spring-boot-starter-mail:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-validation:${springBootVersion}") 
    testImplementation("org.springframework.boot:spring-boot-starter-test:${springBootVersion}")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("com.nimbusds:nimbus-jose-jwt:9.25.6")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.postgresql:postgresql:runtime")
    implementation("org.postgresql:postgresql:42.4.0")
    implementation("com.google.cloud:google-cloud-storage:2.10.0")
    implementation("org.apache.logging.log4j:log4j-api:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-web:${log4jVersion}")
    implementation("com.google.cloud:libraries-bom:25.4.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("com.google.cloud:google-cloud-storage:2.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:${mockito}")
    testImplementation("org.mockito:mockito-core:${mockito}")
    testImplementation("org.mockito:mockito-inline:${mockito}")
    testImplementation("junit:junit:4.13.1")
    //implementation("org.apache.logging.log4j:log4j-slf4j-impl:${log4jVersion}")
}