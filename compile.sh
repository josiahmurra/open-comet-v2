#!/bin/bash

# Create directories if they don't exist
mkdir -p build
mkdir -p dist

# Set ImageJ JAR path and Java path
IMAGEJ_JAR="/Applications/Fiji/jars/ij-1.54p.jar"
JAVA_BIN="/Applications/Fiji/java/macos-arm64/zulu8.84.0.15-ca-fx-jdk8.0.442-macosx_aarch64/bin"

# Copy plugins.config
cp plugins.config build/

# Compile Java files using Fiji's Java
$JAVA_BIN/javac -cp "$IMAGEJ_JAR" -d build *.java

# Create JAR file
cd build
$JAVA_BIN/jar cfm ../dist/OpenComet_v2.0.jar ../manifest.txt *.class plugins.config
cd ..

echo "Build complete. JAR file created in dist/OpenComet_v2.0.jar" 