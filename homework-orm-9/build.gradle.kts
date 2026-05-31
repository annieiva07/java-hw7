plugins {
    `java-library`
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":orm"))
}

application {
    mainClass.set("Main")
}
