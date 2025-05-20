#!/bin/bash
mkdir -p classes
mkdir -p jars

# Compile all Java files
javac -classpath $(hadoop classpath) -d classes *.java

# Create a single JAR with all examples
jar -cvf jars/mapreduce-examples.jar -C classes .

# Clean up
echo "Compilation complete. JAR file created at jars/mapreduce-examples.jar"