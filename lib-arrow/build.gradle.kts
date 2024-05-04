import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
  `java-library`
  id("com.bmuschko.docker-remote-api") version "9.3.0"
}

dependencies {
  // no versions on libraries
  implementation(kotlin("reflect"))
  implementation(libs.arrowCore)
  implementation(libs.kotlinLoggingJvm)
  implementation(libs.quiver)

  testImplementation(libs.junitApi)
  testImplementation(libs.kotestAssertions)
  testImplementation(libs.kotestAssertionsArrow)
  testImplementation(libs.kotestJunitRunnerJvm)
  testImplementation(libs.kotestAssertions)
  testImplementation(libs.kotestJunitRunnerJvm)
  testImplementation(libs.kotestProperty)

  testRuntimeOnly(libs.slf4jSimple)
  testRuntimeOnly(libs.junitEngine)

  apply(plugin = libs.plugins.dokka.get().pluginId)
}

tasks.withType<DokkaTask>().configureEach {
  dokkaSourceSets {
    named("main") {
      moduleName.set("Kotlin FSM")

      // Includes custom documentation
      includes.from("module.md")

      // Points source links to GitHub
      sourceLink {
        localDirectory.set(file("src/main/kotlin"))
        remoteUrl.set(URL("https://github.com/cashapp/kfsm/tree/master/lib-arrow/src/main/kotlin"))
        remoteLineSuffix.set("#L")
      }
    }
  }
}
