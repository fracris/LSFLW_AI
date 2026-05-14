# LittleStarsForLittleWars-ASP

A Java project inspired by *Little Stars for Little Wars* that integrates [EmbASP](https://github.com/DeMaCS-UNICAL/EmbASP-Java) to use **Answer Set Programming (ASP)** through graphical interfaces and logical reasoning engines.

The project features three difficulty levels where the player faces an ASP-based AI opponent. As the difficulty increases, the AI becomes stronger and gains more advanced abilities.

## Dependencies

The project uses **Maven** to manage dependencies. One of the main libraries is **EmbASP**, which is not available on Maven Central but can be imported through [JitPack](https://jitpack.io).

---

## Recommended Method: Dependency via JitPack

Make sure your `pom.xml` contains:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.DeMaCS-UNICAL</groupId>
  <artifactId>EmbASP-Java</artifactId>
  <version>v7.1.0</version>
</dependency>
```

Then run:

```bash
mvn clean install -U
```

> Note: the version **must be `v7.1.0`** with the `v`, because it matches the exact release tag on the GitHub repository.

---

## If JitPack Does Not Work: Manual Alternative

If Maven cannot download the dependency from JitPack, for example because of repository build issues or access problems, you can install the library manually.

### 1. Download the `embasp-7.1.0.jar` file

Download it from the *Releases* section of the repository:  
🔗 https://github.com/DeMaCS-UNICAL/EmbASP-Java/releases/tag/v7.1.0

### 2. Place it inside the `libs/` folder of the project

Create the folder if it does not already exist.

Example:

```text
LittleStars-AI/
├── libs/
│   └── embasp-7.1.0.jar
```

### 3. Install the library into your local Maven repository

Run the following command:

```bash
mvn install:install-file \
  -Dfile=libs/embasp-7.1.0.jar \
  -DgroupId=it.unical.mat \
  -DartifactId=embasp \
  -Dversion=7.1.0 \
  -Dpackaging=jar
```

### 4. Update the `pom.xml` to use the locally installed version

```xml
<dependency>
  <groupId>it.unical.mat</groupId>
  <artifactId>embasp</artifactId>
  <version>7.1.0</version>
</dependency>
```

---

## Final Notes

- This manual approach **works only locally**, so every developer who clones the project must run the same command.
- Alternatively, you can use the `.jar` file directly with `systemPath`, but this approach is **not recommended**.

---

## Project Status
 
This repository contains the final archived version, a university project developed for academic purposes.
 
The project is not currently maintained, and the repository has been archived to preserve the final submitted version. The code remains publicly available as part of the project documentation and portfolio. Future work may restart from this codebase if the project is extended or redesigned.
