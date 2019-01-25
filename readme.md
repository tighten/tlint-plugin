## TLint Plugin Alpha

## To build a release

- run the `intellij` -> `buildPlugin` task, this will output `./build/distributions` directory

## To update the build

Open up `build.gradle` and update these lines

```
plugins {
//  For the latest version visit
//  https://plugins.gradle.org/plugin/org.jetbrains.intellij
id 'org.jetbrains.intellij' version '0.4.2' <- paste the latest version
}
```

```
// Update the package version
group 'com.jetbrains'
version '1.0.3' <- PhpStorm 2018.3
version '1.1.1' <- PhpStorm 2019.1 etc...
```

```Intellij {
// Update phpstorm to the current version
    version '2018.3'
    version '2019.1' <- Current PhpStorm
}
```

Now it's ready to be build.
