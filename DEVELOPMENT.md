# Build

Building module locally and making changes to it (this is optional and not intended for users).

## With Gradle

``` bash
gradle clean build
```

## With Eclipse

- Build Eclipse projects:

``` bash
gradle eclipse
```

- Import them into Eclipse

# Release steps

- Update [Android dependencies](android/gradle.properties) and run `gradle clean build -b android/build.gradle`
- Perform testing on Android using "jros2droid"
- Run `gradle clean build` (Windows)
- Close version in gradle.properties
- Run `gradle clean build javadoc`
- Publish
- Open next SNAPSHOT version
- Update CHANGELOG.md with new release (for changelog generation use `git log --format=%s`)
- Commit changes
- Push
- Upload documentation to website
- Update "bootstrap" project