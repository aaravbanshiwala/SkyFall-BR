plugins {
    kotlin("jvm") version "1.9.21"
}

group = "dev.vintage"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.sparkjava.com/repository/maven-public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.9.0")
    implementation(kotlin("stdlib"))
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
}