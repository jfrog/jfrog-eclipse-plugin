# JFrog Eclipse Plugin [![Build status](https://ci.appveyor.com/api/projects/status/3x4apxgugex3b4hp?svg=true)](https://ci.appveyor.com/project/jfrog-ecosystem/jfrog-eclipse-plugin)


JFrog Eclipse plugin adds JFrog Xray scanning of Maven, Gradle and Npm project dependencies to your Eclipse.

# Building and Testing the Sources

To build the plugin sources, please follow these steps:
1. Clone [ide-plugins-common](https://github.com/jfrog/ide-plugins-common).
2. CD to ide-plugins-common.
3. Install ide-plugins-common dependency by running:
```
./gradlew clean install
```
4. Clone the code from git.
5. CD to *jfrog-eclipse-plugin* directory.
6. Build and create the Eclipse plugin zip file by running the following maven command.
After the build finishes, you'll find the zip file in the *releng/update-site/target/com.jfrog.ide.eclipse.releng.update-site-x.y.x.zip* directory, located under the *jfrog-eclipse-plugin* directory.
The zip file can be loaded into Eclipse.
```
mvn clean package
```
7. If you'd like run the *jfrog-eclipse-plugin* integration tests, run the following command:
```
mvn clean verify
```

# Developing the Plugin Code
If you'd like to help us develop and enhance the plugin, this section is for you.
To build and run the plugin following your code changes, follow these steps:

1. From Eclipse, open all projects under jfrog-eclipse-plugin.
2. From Eclipse, open *jfrog-eclipse-plugin/pom.xml*. Here you'll see error on missing m2e connectors. Install them, as suggested.
3. After restart, open *jfrog-eclipse-plugin/releng/jfrog-target/com.jfrog.ide.eclipse.relen.jfrog-target.target*. Click on "Set as active target platform" and wait until all dependencies resolved.
4. Select all projects in the Package Explorer and click on refresh (F5).
5. Right click on *bundle* project, *Maven --> Update* Project. In the opened *Update Maven Projects* form, select all Maven projects and click *ok*.
6. Open the problems view and make sure absence of errors. If there are errors, repeat steps 3-5.
7. Click on *Run --> Run Configurations* and create a new *Eclipse Application* configuration.
	A. Under *Main --> Workspace Data* check *clear*. 
	B. Under *Configuration --> Configuration Data* check *Clear the configuration area before launching*.
8. To run the Sandbox, click on run under *Run Configurations* or *Run --> Run as --> YOUR_CONFIGURATION_NAME*.

# Code Contributions
We welcome community contribution through pull requests.

# Using JFrog Eclipse plugin
To learn how to use JFrog Eclipse plugin, please visit the [JFrog Eclipse Plugin User Guide](https://www.jfrog.com/confluence/display/XRAY/IDE+Integration).

# Release Notes
The release notes are available in [RELEASE.md](RELEASE.md).
