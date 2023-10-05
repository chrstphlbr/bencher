object V {
    const val arrow = "1.2.1"
    const val asm = "9.6"
    const val commonsCSV = "1.8"
    const val commonsIO = "2.11.0"
    const val eclipseJDT = "3.26.0"
    const val jacoco = "0.8.5"
    const val jgrapht = "1.5.1"
    const val jmetal = "5.11"
    const val jmh = "1.32"
    const val junit = "5.7.2"
    const val jvmTarget = "17"
    const val klaxon = "5.5"
    const val log4j2 = "2.16.0"
    const val picocli = "4.6.3"
    const val wala = "1.6.2"
}

plugins {
    kotlin("jvm") version "1.9.10"
    application
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("me.champeau.jmh") version "0.6.6"
}

group = "ch.uzh.ifi.seal"
version = "0.3.0"

application {
//    applicationDefaultJvmArgs = listOf("-Xms6G", "-Xmx8G")
    mainClass.set("ch.uzh.ifi.seal.bencher.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
//    implementation(group: "org.jetbrains.kotlinx", name: "kotlinx-coroutines-core", version: V.kotlinxCoroutines)
    implementation(group = "info.picocli", name = "picocli", version = V.picocli)
    implementation(group = "com.beust", name = "klaxon", version = V.klaxon)
    implementation(group = "io.arrow-kt", name = "arrow-core", version = V.arrow)
    implementation(group = "com.ibm.wala", name = "com.ibm.wala.core", version = V.wala)
    implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = V.log4j2)
    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = V.log4j2)
    implementation(group = "org.ow2.asm", name = "asm", version = V.asm)
    implementation(group = "org.apache.commons", name = "commons-csv", version = V.commonsCSV)
    implementation(group = "commons-io", name = "commons-io", version = V.commonsIO)
    implementation(group = "org.jgrapht", name = "jgrapht-core", version = V.jgrapht)
    implementation(group = "org.openjdk.jmh", name = "jmh-core", version = V.jmh)
    implementation(group = "org.eclipse.jdt", name = "org.eclipse.jdt.core", version = V.eclipseJDT)
    implementation(group = "org.uma.jmetal", name = "jmetal-core",version = V.jmetal)
    implementation(group = "org.uma.jmetal", name = "jmetal-algorithm",version = V.jmetal)
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
    val ret = mutableListOf<String>()
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
            jvmTarget = V.jvmTarget
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = V.jvmTarget
        }
    }

    compileJmhKotlin {
        kotlinOptions {
            jvmTarget = V.jvmTarget
        }
    }

    jmhJar {
        duplicatesStrategy = DuplicatesStrategy.WARN
        // set jmh jar name
        //[archiveBaseName]-[archiveAppendix]-[archiveVersion]-[archiveClassifier].[archiveExtension]
        archiveClassifier.set("jmh")
    }

    jmh {
        jmhVersion.set(V.jmh)
        duplicateClassesStrategy.set(DuplicatesStrategy.EXCLUDE)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "$group"
            artifactId = rootProject.name
            version = version

            from(components["java"])
        }
    }
}
