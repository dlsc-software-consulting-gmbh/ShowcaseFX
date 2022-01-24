#!/bin/bash

# ------ ENVIRONMENT --------------------------------------------------------
# The script depends on various environment variables to exist in order to
# run properly. The java version we want to use, the location of the java
# binaries (java home), and the project version as defined inside the pom.xml
# file, e.g. 1.0-SNAPSHOT.
#
# PROJECT_VERSION: version used in pom.xml, e.g. 1.0-SNAPSHOT
# APP_VERSION: the application version, e.g. 1.0.0, shown in "about" dialog

JAVA_VERSION=17
JAVA_HOME=$JAVA_HOME
PROJECT_VERSION=$PROJECT_VERSION
APP_VERSION=$APP_VERSION
APP_NAME=$APP_NAME
MAIN_JAR="showcasefx-$PROJECT_VERSION.jar"

echo "java home: $JAVA_HOME"
echo "project version: $PROJECT_VERSION"
echo "app version: $APP_VERSION"
echo "app name: $APP_NAME"
echo "main JAR file: $MAIN_JAR"

# ------ SETUP DIRECTORIES AND FILES ----------------------------------------
# Remove previously generated java runtime and installers. Copy all required
# jar files into the input/libs folder.

rm -rfd ./target/java-runtime/
rm -rfd target/installer/

mkdir -p target/installer/input/libs/

cp target/libs/* target/installer/input/libs/
cp target/${MAIN_JAR} target/installer/input/libs/

# ------ REQUIRED MODULES ---------------------------------------------------
# Use jlink to detect all modules that are required to run the application.
# Starting point for the jdep analysis is the set of jars being used by the
# application.

echo "detecting required modules"
detected_modules=`$JAVA_HOME/bin/jdeps \
  --multi-release ${JAVA_VERSION} \
  --ignore-missing-deps \
  --print-module-deps \
  --class-path "target/installer/input/libs/*" \
    target/classes/com/dlsc/showcase/demo/CssShowcaseViewTestApp.class target/classes/com/dlsc/showcase/demo/CssShowcaseViewTestAppLauncher.class`
echo "detected modules: ${detected_modules}"


# ------ MANUAL MODULES -----------------------------------------------------
# jdk.crypto.ec has to be added manually bound via --bind-services or
# otherwise HTTPS does not work.
#
# See: https://bugs.openjdk.java.net/browse/JDK-8221674

manual_modules=jdk.crypto.ec,jdk.management
echo "manual modules: ${manual_modules}"

# ------ RUNTIME IMAGE ------------------------------------------------------
# Use the jlink tool to create a runtime image for our application. We are
# doing this is a separate step instead of letting jlink do the work as part
# of the jpackage tool. This approach allows for finer configuration and also
# works with dependencies that are not fully modularized, yet.

echo "creating java runtime image"
$JAVA_HOME/bin/jlink \
  --no-header-files \
  --no-man-pages  \
  --compress=2  \
  --strip-debug \
  --add-modules "${detected_modules},${manual_modules}" \
  --output target/java-runtime

# ------ PACKAGING ----------------------------------------------------------
# A loop iterates over the various packaging types supported by jpackage. In
# the end we will find all packages inside the target/installer directory.

echo "Creating installer of type DEB"

$JAVA_HOME/bin/jpackage \
  --type "deb" \
  --dest target/installer \
  --input target/installer/input/libs \
  --name "${APP_NAME}" \
  --main-class com.dlsc.showcase.demo.CssShowcaseViewTestAppLauncher \
  --main-jar ${MAIN_JAR} \
  --runtime-image target/java-runtime \
  --app-version ${APP_VERSION} \
  --linux-deb-maintainer dlemmermann@gmail.com \
  --linux-shortcut \
  --vendor "DLSC, Zurich, Switzerland" \
  --copyright "Copyright © 2022 DLSC GmbH"