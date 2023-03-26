Intended for developers and not users.

# Use Eclipse

- Build eclipse projects:

``` bash
gradle eclipse
```

- Import them into Eclipse

# Release steps

- Close version in gradle.properties
- Run `gradle clean build javadoc` (Linux, Windows)
- Publish
- Open next SNAPSHOT version
- Update CHANGELOG.md with new release (for changelog generation use `git log --format=%s`)
- Commit changes
- Push
- Upload documentation to website
