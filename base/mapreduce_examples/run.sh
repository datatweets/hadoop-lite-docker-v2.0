#!/bin/bash

# Set JAR path
JAR_PATH="jars/mapreduce-examples.jar"

# Check which example to run
if [ "$1" = "wordcount" ]; then
  hadoop jar $JAR_PATH CustomWordCount /user/root/wordcount/input /user/root/wordcount/output-custom
elif [ "$1" = "temperature" ]; then
  hadoop jar $JAR_PATH TemperatureFilter /user/root/temperature/input /user/root/temperature/output-custom
elif [ "$1" = "join" ]; then
  hadoop jar $JAR_PATH JoinExample /user/root/join/input /user/root/join/output-custom
else
  echo "Usage: $0 [wordcount|temperature|join]"
  exit 1
fi

# Print results
echo "Job completed. Results:"
hadoop fs -cat $2/part-*