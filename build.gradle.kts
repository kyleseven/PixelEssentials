plugins {
    kotlin("jvm") version "2.1.10"
    id("com.gradleup.shadow") version "9.0.0-beta6"
}

group = "me.kyleseven"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.aikar.co/content/groups/aikar/") {
        name = "aikar"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.29.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
    compilerOptions.javaParameters = true
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("kotlin", "me.kyleseven.pixelessentials.kotlin")
    relocate("co.aikar.commands", "me.kyleseven.pixelessentials.acf")
    relocate("co.aikar.locales", "me.kyleseven.pixelessentials.locales")
    minimize()
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
