@echo off
set DIR=%~dp0

set WRAPPER_JAR=%DIR%gradle\wrapper\gradle-wrapper.jar
set WRAPPER_PROPERTIES=%DIR%gradle\wrapper\gradle-wrapper.properties

java -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
