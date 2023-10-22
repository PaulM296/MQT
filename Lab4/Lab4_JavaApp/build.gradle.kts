val implementation: Any

plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation group: 'org.apache.kafka', name: 'kafka-clients', version: '3.6.0'

    implementation group: 'org.slf4j', name: 'slf4j-api', version: '2.0.9'
    implementation group: 'ch.qos.logback', name: 'logback-core', version: '1.4.11'
    implementation group: 'ch.qos.logback', name: 'logback-classic', version: '1.4.11'
    
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}