plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    jacoco
}

group = "io.github.bazhanovmaxim"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("org.jetbrains:annotations:26.0.1")

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.4")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("FAILED", "SKIPPED")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn("jacocoTestReport", "jacocoTestCoverageVerification")
}

// Манифест для стабильного JPMS имени
tasks.jar {
    manifest {
        attributes(mapOf("Automatic-Module-Name" to "io.github.bazhanovmaxim.functional"))
    }
}

// Javadoc UTF-8 и мягкая проверка
tasks.javadoc {
    (options as? StandardJavadocDocletOptions)?.apply {
        encoding = "UTF-8"
        addBooleanOption("Xdoclint:none", true)
        addStringOption("charset", "UTF-8")
        addStringOption("docencoding", "UTF-8")
    }
}

// Публикация: корректный POM
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "java-functional"

            pom {
                name.set("java-functional")
                description.set("Option · Try · Either for Java with Kotlin-like ergonomics")
                url.set("https://github.com/BazhanovMaxim/java-functional")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/BazhanovMaxim/java-functional/blob/main/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("bazhanovmaxim")
                        name.set("Maxim Bazhanov")
                        url.set("https://github.com/BazhanovMaxim")
                    }
                }
                scm {
                    url.set("https://github.com/BazhanovMaxim/java-functional")
                    connection.set("scm:git:https://github.com/BazhanovMaxim/java-functional.git")
                    developerConnection.set("scm:git:ssh://git@github.com/BazhanovMaxim/java-functional.git")
                }
            }
        }
    }
}

// Подпись in-memory из env
signing {
    val signingKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    } else {
        logger.lifecycle("Signing is disabled (SIGNING_KEY or SIGNING_PASSWORD not set).")
    }
}

// Публикация в Sonatype s01
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))

            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))

            packageGroup.set("io.github.bazhanovmaxim")
        }
    }
}
