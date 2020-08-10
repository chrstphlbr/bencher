object V {
    const val wala = "1.5.0"
    const val aspectj = "1.9.1"
    const val klaxon = "3.0.11"
    const val junit = "5.6.0"
    const val log4j2 = "2.11.1"
    const val funktionale = "1.2"
    const val commonsCSV = "1.6"
    const val commonsIO = "2.6"
    const val asm = "7.2"
    const val picocli = "3.9.2"
//    const val kotlinxCoroutines = "1.1.1"
    const val jgrapht = "1.3.0"
    const val jmh = "1.22"
    const val eclipseJDT = "3.18.0"
    const val jacoco = "0.8.5"
}

plugins {
    kotlin("jvm") version "1.3.72"
    application
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("me.champeau.gradle.jmh") version "0.5.0"
}

group = "ch.uzh.ifi.seal"
version = "1.0-SNAPSHOT"

application {
//    applicationDefaultJvmArgs = listOf("-Xms6G", "-Xmx8G")
    mainClassName = "ch.uzh.ifi.seal.bencher.MainKt"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
//    implementation(group: "org.jetbrains.kotlinx", name: "kotlinx-coroutines-core", version: V.kotlinxCoroutines)
    implementation(group = "info.picocli", name = "picocli", version = V.picocli)
    implementation(group = "com.beust", name = "klaxon", version = V.klaxon)
    implementation(group = "org.funktionale", name = "funktionale-all", version = V.funktionale)
    implementation(group = "com.ibm.wala", name = "com.ibm.wala.core", version = V.wala)
    implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = V.log4j2)
    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = V.log4j2)
    implementation(group = "org.ow2.asm", name = "asm", version = V.asm)
    implementation(group = "org.apache.commons", name = "commons-csv", version = V.commonsCSV)
    implementation(group = "commons-io", name = "commons-io", version = V.commonsIO)
    implementation(group = "org.jgrapht", name = "jgrapht-core", version = V.jgrapht)
    implementation(group = "org.openjdk.jmh", name = "jmh-core", version = V.jmh)
    implementation(group = "org.eclipse.jdt", name = "org.eclipse.jdt.core", version = V.eclipseJDT)
    implementation(group = "org.jacoco", name = "org.jacoco.core", version = V.jacoco)
    implementation(group = "org.jacoco", name = "org.jacoco.report", version = V.jacoco)
    jmh(group = "org.openjdk.jmh", name = "jmh-generator-annprocess", version = V.jmh)

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = V.junit)
    testImplementation(group = "org.junit.jupiter", name =  "junit-jupiter-params", version = V.junit)
    testImplementation(kotlin("test"))
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = V.junit)
}

fun splitWith(c: Char, clargs: String): List<String> {
    var inC = false
    var ret = mutableListOf<String>()
    var curr = ""

    clargs.forEach { i ->
        if (i == c) {
            inC = !inC
        } else if (i == ' ' && !inC) {
            ret.add(curr)
            curr = ""
        } else {
            curr += i
        }
    }

    if (curr != "") {
        ret.add(curr)
    }

    return ret
}

tasks {
    defaultTasks("run")

    named<JavaExec>("run") {
        val clargs = System.getProperty("args")
        if (clargs != null) {
            args(when {
                clargs.contains("\"") -> splitWith('"', clargs)
                clargs.contains("\'") -> splitWith('\'', clargs)
                else -> clargs.split(" ")
            })
        }
    }

    test {
        useJUnitPlatform()
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    jmhJar {
        duplicatesStrategy = DuplicatesStrategy.WARN
        // set jmh jar name
        //[archiveBaseName]-[archiveAppendix]-[archiveVersion]-[archiveClassifier].[archiveExtension]
        //TODO add classifier to JAR
//        archiveClassifier = "jmh"
    }

    jmh {
        jmhVersion = V.jmh
        duplicateClassesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "$group"
            artifactId = "${rootProject.name}"
            version = "$version"

            from(components["java"])
        }
    }
}
