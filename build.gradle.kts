plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.8.0"
}

group = "me.entityparrot"
version = "1.0.3"

repositories {
    mavenCentral()
    // maven("https://repo.tabooproject.org/repository/releases")
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://ptms.ink:8081/repository/releases")
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-util:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("org.tabooproject.reflex:fast-instance-getter:1.0.19")
    implementation("org.tabooproject.reflex:analyser:1.0.19")
    implementation("org.tabooproject.reflex:reflex:1.0.19")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.tabooproject.org/repository/releases")
            credentials {
                username = project.findProperty("taboolibUsername").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            afterEvaluate {
                artifactId = tasks.jar.get().archiveBaseName.get()
            }
        }
    }
}