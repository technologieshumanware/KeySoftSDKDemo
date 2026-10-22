import java.util.Properties

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

fun tokenFromKeystore(propertyKey: String): String {
    var token = ""
    val keystorePropertyFile = file("keystore.properties")

    if (keystorePropertyFile.exists()) {
        val keystoreProperties = Properties()
        keystoreProperties.load(keystorePropertyFile.reader())
        if (keystoreProperties.containsKey(propertyKey)) {
            token = keystoreProperties.getProperty(propertyKey)
        }
    }

    return token
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        //mavenLocal()
        google()
        mavenCentral()
        maven {
            url = uri(extra["gitlab_maven_repo_url"].toString())
            credentials(HttpHeaderCredentials::class) {
                name = "Deploy-Token"
                value = tokenFromKeystore("gitlab_maven_repo_deployToken")
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}
rootProject.name = "KeySoftSDK Demo App"
include (":app")
