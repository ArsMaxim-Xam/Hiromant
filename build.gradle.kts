// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

tasks.register("incrementVersion") {
  notCompatibleWithConfigurationCache("Writes external files")
  doLast {
    val versionFile = file("version.txt")
    val currentVersion = if (versionFile.exists()) versionFile.readText().trim() else "1.001"
    val currentDouble = currentVersion.toDoubleOrNull() ?: 1.001
    val nextDouble = currentDouble + 0.001
    val nextVersion = String.format(java.util.Locale.US, "%.3f", nextDouble)
    versionFile.writeText(nextVersion)

    // Update metadata.json name field
    val metadataFile = file("metadata.json")
    if (metadataFile.exists()) {
      val content = metadataFile.readText()
      val updated = content.replace(Regex("\"name\"\\s*:\\s*\".*?\""), "\"name\": \"Хиромант $nextVersion\"")
      metadataFile.writeText(updated)
    }

    println("SUCCESS: Version incremented from $currentVersion to $nextVersion")
  }
}
