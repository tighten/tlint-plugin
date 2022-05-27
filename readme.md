## TLint Plugin Alpha

## To update the build

Open up `gradle.properties` and update these lines

```
myPluginVersion=1.2.3 // increment this
intellijPlatformVersion=2021.2 // update this to latest stable
```

Open up `build.gradle.kts` and update these lines

```
plugins {
//  For the latest version visit
//  https://plugins.gradle.org/plugin/org.jetbrains.intellij
id("org.jetbrains.intellij") version "1.6.0" <- paste the latest version
}
```

## To build a release

```
// Click Gradle on right side of editor to find intelliji or Choose View > Tool Windows > Gradle
```
- run the `intellij` -> `buildPlugin` task, this will output `./build/distributions` directory

Now it's ready to be built and tested.

## Updating Gradle Wrapper

You can update the grade wrapper by running

```shell
./gradlew wrapper --gradle-version=VERSION
```

## Troubleshooting

If you get errors when building try to run `Tasks/build/clean` before reattempting.