# Shenji

Shenji is a Fabric modding project for Minecraft Java Edition 26.3 and JDK 25. The repository contains one Gradle build with two subprojects.

| Subproject | Fabric mod ID | Role |
| --- | --- | --- |
| `framework` | `shenji_framework` | Shared technical library with an empty Fabric initializer |
| `realms` | `shenji_realms` | Mod that requires Framework |

## Develop in IntelliJ IDEA

Open the repository root as a Gradle project. Set the Project SDK and Gradle JVM to JDK 25, select the Gradle wrapper, and sync. Both subprojects are available in the same IDEA project. Launch the Loom-generated
`Minecraft Client (:realms)` configuration or run the Realms `runClient` task from the Gradle tool window.

From the repository root:

```sh
./gradlew build
./gradlew :realms:runClient
./gradlew :realms:dependencies --configuration runtimeClasspath
```

On Windows, use `gradlew.bat`. Client data lives in `realms/run/`.

## Dependencies and distribution

`gradle.properties` pins Minecraft, Fabric Loader, Fabric Loom, Java, and the shared mod version. The root build applies the same Java toolchain and resource processing to both mods. Resource processing expands the versions in each `fabric.mod.json`. Fabric Loader provides the initializer interface, so these skeletons need only Loader.

Realms declares `implementation project(':framework')` for compilation and development runtime. Its `fabric.mod.json` separately requires `shenji_framework` at the shared mod version or newer. Loom makes Framework available on the Realms client classpath, and Fabric Loader discovers both mods at launch.

Build outputs are `framework/build/libs/shenji-framework-<version>.jar` and `realms/build/libs/shenji-realms-<version>.jar`. Install both JARs in the Fabric instance's `mods` directory to run Realms. Framework can be installed independently.

The build uses Loom's non-remapping plugin for Minecraft 26.3. The standard Gradle `jar` task produces each distribution JAR. See the [official Fabric 26.3 example](https://github.com/FabricMC/fabric-example-mod/tree/26.3) and the [Fabric Loom documentation](https://docs.fabricmc.net/develop/loom/).
