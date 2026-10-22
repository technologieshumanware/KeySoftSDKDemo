# Getting started

This sample application aims to demonstrate some of the basic features 
provided by Humanware's KeySoftSDK for use in developing applications for the
Monarch. You may simply run this application on your development setup in order
to try out what the SDK offers. For more detailed information on each activity
in this app, please refer to the demo's source code provided with this document.

You should have received a token to access Humanware's Maven repository for the SDK.
Ensure this token is specified in a file called `keystore.properties` located in the
root folder of this demo app. The file should contain the following:

```
gitlab_maven_repo_deployToken=[your token here]
```

`keystore.properties` should not be checked into version control,
in order to accommodate for different local development environment configurations.

To add KeySoftSDK as a dependency to your repository, you need to add a `gradle.properties` like the one
in this project. You need to specify at least this line :

```
gitlab_maven_repo_url=https://gitlab.com/api/v4/projects/45979364/packages/maven
```

With the following maven repository in the `settings.gradle` (the method `tokenFromKeystore` is from the same file):

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
       [...]
        maven {
            url gitlab_maven_repo_url
            credentials(HttpHeaderCredentials) {
                name = "Deploy-Token"
                value = tokenFromKeystore("gitlab_maven_repo_deployToken")
            }
            authentication {
                header(HttpHeaderAuthentication)
            }
        }
    }
}
```

## Optional modules

The image viewer (`ImageViewerWidget`) is published as a separate artifact. Add it next to
`KeySoftSDK` if your app displays PDF, SVG or raster images:

```
dependencies {
    implementation 'com.humanware:KeySoftSDK:latest.release'
    implementation 'com.humanware:KeySoftSDK-imageviewer:latest.release'
}
```

See the [image viewer tutorial](./tutorials/image-viewer-widget.md) for details.

## Build configuration language

Since using Kotlin DSL as a gradle scripting language has become the norm, two solutions are provided: Groovy DSL and Kotlin DSL. In this way, the gradle scripts of both build languages are bundled together:
* build.gradle and build.gradle.kts,
* settings.gradle and settings.gradle.kts

In this demonstration project, Android Studio uses the Groovy language by default, just delete the Groovy files (build.gradle + settings.gradle) if you want to use kotlin.