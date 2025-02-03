plugins {
    kotlin("jvm") version "2.1.10"
    id("com.gradleup.shadow") version "9.0.0-beta6"
    id("org.jooq.jooq-codegen-gradle") version "3.19.18"
    id("org.flywaydb.flyway") version "11.3.0"
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
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    // Database
    implementation("org.xerial:sqlite-jdbc:3.48.0.0")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.jooq:jooq:3.19.18")
    implementation("org.flywaydb:flyway-core:11.3.0")

    // Jooq Codegen
    jooqCodegen("org.xerial:sqlite-jdbc:3.48.0.0")
    jooqCodegen("org.jooq:jooq-meta:3.19.18")
    jooqCodegen("org.jooq:jooq-codegen:3.19.18")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.29.1")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
    compilerOptions.javaParameters = true
}

val dbFile = "database.db"
val jdbcUrl = "jdbc:sqlite:$rootDir/$dbFile"
jooq {
    configuration {
        jdbc {
            driver = "org.sqlite.JDBC"
            url = jdbcUrl
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.sqlite.SQLiteDatabase"
                excludes = "flyway_schema_history"
            }
            target {
                packageName = "me.kyleseven.pixelessentials.database.generated"
                directory = "src/main/kotlin"
            }
        }
    }
}

flyway {
    url = jdbcUrl
}

tasks.jooqCodegen {
    dependsOn("flywayMigrate")
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
