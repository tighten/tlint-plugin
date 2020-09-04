## TLint Plugin Alpha

## To update the build

Open up `gradle.properties` and update these lines
```
myPluginVersion=1.1.7 // increment this
intellijPlatformVersion=2020.2 // update this to latest stable
```

Open up `build.gradle` and update these lines

```
plugins {
//  For the latest version visit
//  https://plugins.gradle.org/plugin/org.jetbrains.intellij
id 'org.jetbrains.intellij' version '0.4.22' <- paste the latest version
}
```

## To build a release

- run the `intellij` -> `buildPlugin` task, this will output `./build/distributions` directory

Now it's ready to be built and tested.
