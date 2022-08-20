Intended for developers and not users.

# Use Eclipse

- Build eclipse projects:

``` bash
gradle eclipse
```

- Import them into Eclipse

# Release steps

- Close version in gradle.properties
- Run `gradle clean build javadoc`
- Publish
- Open next SNAPSHOT version
- Commit changes
- Push
- Create new release in GitHub
- Upload documentation to website
