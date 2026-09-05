import org.gradle.api.file.DuplicatesStrategy

plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
    id("org.jooq.jooq-codegen-gradle") version "3.21.7"
    id("org.flywaydb.flyway") version "13.5.0"
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
    maven("https://jitpack.io") {
        name = "jitpack"
    }
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:26.2.build.121-stable")
    compileOnly("org.apache.logging.log4j:log4j-api:2.26.0")
    compileOnly("org.apache.logging.log4j:log4j-core:2.26.0")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    // Vault
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    // Database
    compileOnly("org.xerial:sqlite-jdbc:3.53.4.0")
    compileOnly("org.jooq:jooq:3.21.7")
    compileOnly("org.flywaydb:flyway-core:13.5.0")

    // Jooq Codegen
    jooqCodegen("org.xerial:sqlite-jdbc:3.53.4.0")
    jooqCodegen("org.jooq:jooq-meta:3.21.7")
    jooqCodegen("org.jooq:jooq-codegen:3.21.7")
}

val targetJavaVersion = 25
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
                forcedTypes {
                    forcedType {
                        name = "BIGINT"
                        userType = "kotlin.Long"
                        includeExpression = ".*_id"
                        includeTypes = "(?i:INTEGER)"
                    }
                }
            }
            generate {
                isKotlinNotNullPojoAttributes = true
                isKotlinNotNullRecordAttributes = true
                isKotlinNotNullInterfaceAttributes = true
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
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    relocate("co.aikar.commands", "me.kyleseven.pixelessentials.acf")
    relocate("co.aikar.locales", "me.kyleseven.pixelessentials.locales")

    mergeServiceFiles()

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
