#!/usr/bin/env sh

DIR="$(cd "$(dirname "$0")" && pwd)"

WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="$DIR/gradle/wrapper/gradle-wrapper.properties"

exec java -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
