plugins {
    java
    application
    id("maven-publish")
}

group = "io.rsug"
version = "0.0.1"

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
//    implementation("javax.xml.bind:jaxb-api:2.3.1")
//    implementation("com.sun.xml.bind:jaxb-impl:2.3.1")
//    implementation("com.sun.xml.bind:jaxb-core:2.3.0.1")
    implementation("io.rsug:komar:0.0.1")
    implementation("commons-io:commons-io:2.22.0")
    implementation("org.ow2.asm:asm:9.10.1")
    implementation("org.ow2.asm:asm-commons:9.10.1")
    implementation("org.ow2.asm:asm-util:9.10.1")
    implementation("org.ow2.asm:asm-tree:9.10.1")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
    manifest {
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "io.rsug.kopalka.Main"
    }
}


tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
