# Contributing

If you would like to contribute code to this project you can do so through GitHub by forking the repository and sending
a pull request.

When submitting code, please make every effort to follow existing conventions and style in order to keep the code as
readable as possible.

Before your code can be accepted into the project you must also sign the
[Individual Contributor License Agreement (CLA)][1].

## Building

KFSM uses CashApp's [Hermit](https://cashapp.github.io/hermit/). Hermit ensures that your team, your contributors,
and your CI have the same consistent tooling. Here are the [installation instructions](https://cashapp.github.io/hermit/usage/get-started/#installing-hermit).

[Activate Hermit](https://cashapp.github.io/hermit/usage/get-started/#activating-an-environment) either
by [enabling the shell hooks](https://cashapp.github.io/hermit/usage/shell/) (one-time only, recommended) or manually
sourcing the env with `. ./bin/activate-hermit`.

Use gradle to run all tests

```shell
gradle build
```

## Breaking changes

We use the [Kotlin binary compatibility validator][2] to check for API changes. If a change contains an API change and
breaks the build, run the `:apiDump` task and commit the resulting changes to the `.api` files. `.api` files should not
have removals and additions in the same change so that downstream apps do not immediately run into
backwards-compatibility issues.

## Upgrading dependencies

Dependency versions are listed in the Gradle catalog file: gradle/libs.versions.toml.

To check for dependencies to update:

```shell
 gradle dependencyUpdates -Drevision=release
```

Dependencies can be updated by editing the catalog file.

The version catalog update plugin can also format and update the catalog file.  See the documentation for more detail.

Updating all available dependencies:

```shell
gradle versionCatalogUpdate
```

Gradle Versions Plugin: https://github.com/ben-manes/gradle-versions-plugin
Version Catalog Update Plugin: https://github.com/littlerobots/version-catalog-update-plugin

[1]: https://spreadsheets.google.com/spreadsheet/viewform?formkey=dDViT2xzUHAwRkI3X3k5Z0lQM091OGc6MQ&ndplr=1

[2]: https://github.com/Kotlin/binary-compatibility-validator

