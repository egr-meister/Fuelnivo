The binary gradle-wrapper.jar is intentionally not committed here because it is
a build artifact. Generate it once with an installed Gradle 8.9+:

    gradle wrapper --gradle-version 8.11.1

Android Studio also regenerates it automatically when you open the project.
The GitHub Actions workflow generates it before building.
