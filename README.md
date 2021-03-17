# Hedvig [GitHub Packages](https://github.com/features/package-registry) repo for shared libs

This is the new "Hedvig Maven Central" replacing Bintray for shared libs at Hedvig.

Published packages can be found [here](https://github.com/orgs/HedvigInsurance/packages).

## Add dependency

To include any lib from this repo into your project you have to do three things:

1. Add the lib as dependency in your `pom.mxl`.
```
<dependency>
    <groupId>com.hedvig</groupId>
    <artifactId>[lib-name]</artifactId>
    <version>[lib-version]</version>
</dependency>
```
2. Add this repository in your `pom.xml`:
```
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Hedvig Maven Packages</name>
        <url>https://maven.pkg.github.com/HedvigInsurance/libs</url>
        <releases><enabled>true</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
```

3. Add the authentication to the Package Registry to your global `USER_HOME\.m2\settings.xml`:
```
<servers>
    <server>
        <id>github</id>
        <username>YOUR_USER_NAME</username>
        <password>YOUR_AUTH_TOKEN</password>
    </server>
</servers>
```
Replace the `YOUR_USER_NAME` with your GitHub user name and replace `YOUR_AUTH_TOKEN` with a generated GitHub personal access token:
GitHub > Settings > Developer Settings > Personal access tokens > Generate new token:
The token needs at least the read:packages scope.

## Deploy libs
To deploy a lib into this repo make sure you have added the `maven-deploy-plugin` to your `pom.xml`:
```
<plugin>
    <artifactId>maven-deploy-plugin</artifactId>
    <version>2.8.2</version>
</plugin>
```
To deploy a new version:
```
mvn deploy
```
