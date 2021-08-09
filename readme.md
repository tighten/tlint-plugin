## TLint Plugin Alpha

## To update the build

Open up `gradle.properties` and update these lines
```
myPluginVersion=1.2.3 // increment this
intellijPlatformVersion=2021.2 // update this to latest stable
```

Open up `build.gradle` and update these lines

```
plugins {
//  For the latest version visit
//  https://plugins.gradle.org/plugin/org.jetbrains.intellij
id 'org.jetbrains.intellij' version '0.7.3' <- paste the latest version
}
```

## To build a release

```
// Click Gradle on right side of editor to find intelliji
```
- run the `intellij` -> `buildPlugin` task, this will output `./build/distributions` directory

Now it's ready to be built and tested.
