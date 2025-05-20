# MapReduce on Hadoop (Docker-based Environment)
#### Author: Mehdi Lotfinejad 
## Overview

In this comprehensive tutorial, you'll learn how to run MapReduce jobs on a Hadoop cluster running within Docker containers. We'll cover fundamental concepts, detailed explanations, and hands-on exercises using provided Java examples.

------

# Section 1: Setting Up the Environment (Prerequisites)

In this section, you'll set up a Hadoop environment running on Docker. This environment will allow you to run MapReduce jobs seamlessly and reliably. Let's walk through each step carefully, making sure nothing is skipped.

------

## Step 1: Ensure Docker is Installed

First, confirm Docker is installed on your system. If you haven't installed Docker yet, follow these official instructions based on your operating system:

- [Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/)
- [Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/)
- [Docker Engine for Linux](https://docs.docker.com/engine/install/)

Once Docker is installed, open your terminal (Mac/Linux) or command prompt/PowerShell (Windows) and verify Docker installation by running:

```bash
docker --version
```

You should see output similar to this:

```
Docker version 28.0.0, build 123abc
```

------

## Step 2: Clone the GitHub Repository

Next, clone the provided Hadoop Docker setup from your GitHub repository to your local machine. Run the following command:

```bash
git clone https://github.com/datatweets/hadoop-lite-docker.git
```

Then, navigate into the cloned repository directory:

```bash
cd hadoop-lite-docker
```

Verify that the contents are correctly downloaded by listing the directory content:

```bash
ls -la
```

You should see files like `docker-compose.yml`, along with directories containing Hadoop configurations.

------

## Step 3: Start Hadoop Environment using Docker Compose

The provided repository includes a Docker Compose file (`docker-compose.yml`) that will deploy a complete Hadoop environment with these services:

- **NameNode**: Manages the filesystem namespace
- **DataNode(s)**: Stores the actual data
- **ResourceManager** and **NodeManager**: Manages resources for YARN (MapReduce framework)
- **HistoryServer**: Provides job history management and tracking

Start your Hadoop environment by running the following Docker Compose command:

```bash
docker compose up -d
```

The `-d` flag runs containers in the background (detached mode).

This process may take a few minutes to download and set up all containers, especially the first time.

------

## Step 4: Verify Hadoop Components are Running

After Docker Compose completes, ensure all containers are running successfully:

```bash
docker ps | grep -E 'namenode|datanode|resourcemanager|nodemanager|historyserver'
```

You should see a list similar to this (your exact container IDs and ports might vary):

```
CONTAINER ID   IMAGE                                 COMMAND                  STATUS              PORTS
123abc456def   fjardim/namenode_sqoop                "/entrypoint.sh"         Up 2 minutes        0.0.0.0:50070->50070/tcp
789ghi012jkl   fjardim/datanode                      "/entrypoint.sh"         Up 2 minutes
345mno678pqr   bde2020/hadoop-resourcemanager        "/entrypoint.sh"         Up 2 minutes        0.0.0.0:8088->8088/tcp
901stu234vwx   bde2020/hadoop-nodemanager            "/entrypoint.sh"         Up 2 minutes
567yz123abc4   bde2020/hadoop-historyserver          "/entrypoint.sh"         Up 2 minutes        0.0.0.0:19888->19888/tcp
```

------

## Step 5: Access Hadoop Web Interfaces (Optional)

Hadoop provides web-based interfaces for monitoring and managing the cluster:

- **NameNode UI**: Check filesystem status
  [http://localhost:50070](http://localhost:50070/)
- **ResourceManager UI**: Monitor YARN applications and jobs
  [http://localhost:8088](http://localhost:8088/)
- Open these links in your browser to confirm all services are operational and accessible.

------

## Step 6: Access the NameNode Container Shell

Most of your MapReduce exercises will be conducted from within the NameNode container shell. To access the shell, run:

```bash
docker exec -it namenode bash
```

After entering the container, your command prompt should change to indicate you are inside the container (something similar to):

```bash
root@namenode:/#
```

From now on, all Hadoop-related commands will be executed from within this container.

------

## Step 7: Set Up the Working Directories in NameNode Container

Once you're inside the NameNode container, create the necessary directories for your MapReduce samples:

```bash
# Create local directory for datasets
mkdir -p /tmp/mapreduce_samples

# Create directory for Java examples and scripts
mkdir -p /opt/mapreduce_examples
```

------

## Final Checkpoint:

At the end of this setup process, your environment should be ready with:

- Docker and Hadoop cluster running correctly.
- Verified web interfaces operational.
- Accessible shell inside the NameNode container.
- Directories prepared for placing input files and Java codes.

Your environment is now fully prepared for the next sections, where you'll begin understanding and running MapReduce jobs.

------

### Next Steps:

This next section will cover fundamental MapReduce concepts and architecture in detail, preparing you to effectively run and customize your MapReduce jobs.



---



# Section 2: Understanding MapReduce

In this section, you'll learn fundamental MapReduce concepts and architecture. A strong theoretical understanding of MapReduce is essential before diving into practical examples. Let's explore the components in detail.

------

## What is MapReduce?

MapReduce is a distributed programming model designed to process large datasets efficiently across multiple machines. Originally developed by Google, it became popular through Apache Hadoop. The primary strength of MapReduce lies in its ability to scale horizontally, processing massive amounts of data in parallel.

### MapReduce Workflow

The MapReduce process consists of three main phases:

1. **Map Phase**
2. **Shuffle and Sort Phase**
3. **Reduce Phase**

Let's discuss each phase in detail.

------

## 1. Map Phase

In the **Map Phase**, data is split into manageable chunks and distributed to individual mapper tasks running on cluster nodes.

### Mapper Role:

- Processes input data line by line.
- Transforms input into intermediate key-value pairs.

### Example Pseudocode (Mapper):

```pseudo
function Mapper(input_key, input_value):
    for each word in input_value:
        emit(word, 1)
```

- **Explanation**: For a WordCount example, the mapper takes each line (the `input_value`), splits it into words, and outputs `(word, 1)` pairs.

------

## 2. Shuffle and Sort Phase

After the mapper emits intermediate key-value pairs, the **Shuffle and Sort Phase** starts automatically and implicitly:

- All intermediate key-value pairs emitted by mappers are grouped by their keys.
- Sorted keys are redistributed across reducers, ensuring that all pairs with the same key end up at the same reducer.

### Why Shuffle and Sort?

This phase ensures that each reducer receives a coherent set of data (grouped by keys) for aggregation. It is managed internally by Hadoop.

### Optimization Techniques (Brief Mention):

- **Combiner**: Optional component to minimize data transfer by partially aggregating mapper outputs locally.
- **Partitioner**: Determines the reducer to which each key-value pair is sent, balancing the load evenly.

------

## 3. Reduce Phase

In the **Reduce Phase**, reducers take the grouped intermediate data and process it to produce the final output.

### Reducer Role:

- Receives sorted keys with corresponding list of values.
- Performs aggregation or summarization operations.
- Emits the final key-value pairs as output.

### Example Pseudocode (Reducer):

```pseudo
function Reducer(intermediate_key, list_of_values):
    sum = 0
    for each value in list_of_values:
        sum = sum + value
    emit(intermediate_key, sum)
```

- **Explanation**: For WordCount, the reducer receives a list of values (`[1,1,1,...]`) associated with each word, sums them, and emits `(word, total_count)`.

------

## MapReduce Components Explained

### 1. Mapper

- Reads input data
- Produces intermediate key-value pairs
- Typically performs filtering, parsing, and transformation tasks

### 2. Reducer

- Receives intermediate key-value pairs grouped by keys
- Aggregates values to generate final results
- Tasks include summation, counting, joining, averaging

### 3. Combiner (Optional, but Recommended)

- Performs local aggregation to reduce network traffic
- Usually implements the same logic as the reducer (but intermediate)

### 4. Partitioner

- Determines how intermediate keys are partitioned across reducers
- Balances reducer workload
- Custom partitioners optimize data distribution

------

## Common MapReduce Design Patterns:

### 1. Filtering:

- Mapper emits only records matching specific criteria.
- Reducer may consolidate or further process filtered data.

### 2. Aggregation:

- Counting occurrences, calculating sums or averages.
- Example: WordCount or calculating average temperature.

### 3. Joining Datasets:

- Combining multiple datasets based on common keys.
- Map phase tags records from each dataset.
- Reduce phase matches keys to perform the join operation.

------

## Input and Output Formats & Serialization:

Hadoop provides several built-in input and output formats:

- **TextInputFormat**: Standard line-by-line input processing.
- **KeyValueTextInputFormat**: Key-value pairs separated by tab characters.
- **SequenceFileInputFormat**: Binary input, efficient for large-scale processing.

For data serialization, Hadoop uses:

- **Writable** and **WritableComparable** interfaces.
- Commonly used types: `IntWritable`, `LongWritable`, `Text`.
- Custom writable types can be implemented for efficiency.

------

## Shuffle/Sort Optimization Techniques (Advanced Considerations):

Efficient shuffle/sort operations significantly improve MapReduce performance. Key optimization strategies include:

- **Using Combiners**: Minimize data transferred from mappers to reducers.
- **Custom Partitioners**: Control data distribution and workload balance.
- **Adjusting Hadoop Configuration**:
  - Increasing memory allocation (`mapreduce.task.io.sort.mb`)
  - Tuning the number of reducers for optimal parallelism

------

## Summary of MapReduce Process (Simplified Flow):

```plaintext
Input Data → [Split] → Mapper → [Intermediate Key-Value] → Shuffle/Sort → Reducer → Final Output
```

- Input: Raw data split across mappers.
- Mapper: Processes and emits intermediate key-value pairs.
- Shuffle/Sort: Automatically groups and sorts intermediate data.
- Reducer: Aggregates intermediate data into final output.

------

## Recap of Important Concepts:

- **Mapper**: Data transformation, filtering, emitting intermediate pairs.
- **Reducer**: Data aggregation, joining, producing final results.
- **Shuffle/Sort**: Automatic Hadoop-managed phase for intermediate data.
- **Combiner and Partitioner**: Optimization components for better performance.

------

## Example: WordCount MapReduce Flow (Illustrative):

Consider the text:

```
Hello Hadoop
Hello MapReduce
```

**Map Phase Output:**

```
("hello", 1), ("hadoop", 1), ("hello", 1), ("mapreduce", 1)
```

**Shuffle/Sort Output (Grouped):**

```
("hadoop", [1]), ("hello", [1,1]), ("mapreduce", [1])
```

**Reduce Phase Output:**

```
("hadoop", 1), ("hello", 2), ("mapreduce", 1)
```

------

## Next Steps:

You've gained foundational knowledge of MapReduce concepts and architecture. Now, you’re ready to apply these theoretical foundations practically.

In the next section, **Section 3: Preparing for Hands-on Exercises**, we will set up datasets and input files required for our MapReduce examples and jobs.



# Section 3: Preparing for Hands-on Exercises

In this section, you'll prepare the input datasets required for your MapReduce hands-on exercises. We’ll create sample data files and directories within Hadoop's file system (HDFS). You'll learn to upload data to HDFS, ensuring everything is correctly configured before running actual MapReduce jobs.

------

## Step-by-Step Guide

Ensure you have completed [Section 1: Setting Up the Environment](#) and you are currently inside the **NameNode container shell**. If not, enter the NameNode container first:

```bash
docker exec -it namenode bash
```

Your command prompt should now appear as:

```bash
root@namenode:/#
```

------

## Step 1: Create Local Directories for Sample Datasets

First, create a local directory to store your sample data files temporarily within the NameNode container:

```bash
mkdir -p /tmp/mapreduce_samples
cd /tmp/mapreduce_samples
```

------

## Step 2: Prepare Sample Data Files

We'll prepare three sample datasets for our MapReduce examples:

### Dataset 1: Word Count Input Data (`input.txt`)

Create a simple text file for the WordCount example:

```bash
cat > input.txt << EOF
Hello Hadoop
Hello MapReduce
Welcome to Hadoop MapReduce
EOF
```

Verify its content:

```bash
cat input.txt
```

Output:

```bash
Hello Hadoop
Hello MapReduce
Welcome to Hadoop MapReduce
```

------

### Dataset 2: Temperature Readings (`temperatures.csv`)

This dataset is for our Temperature Filter example:

```bash
cat > temperatures.csv << EOF
station1,2021-01-01,32.5
station1,2021-01-02,29.5
station2,2021-01-01,30.5
station2,2021-01-02,28.4
station3,2021-01-01,35.0
EOF
```

Verify its content:

```bash
cat temperatures.csv
```

Output:

```bash
station1,2021-01-01,32.5
station1,2021-01-02,29.5
station2,2021-01-01,30.5
station2,2021-01-02,28.4
station3,2021-01-01,35.0
```

------

### Dataset 3: Join Example Datasets (`employees.csv`, `departments.csv`)

We'll use these CSV files for our Join example:

Create `employees.csv`:

```bash
cat > employees.csv << EOF
1,John,101
2,Jane,102
3,Bob,101
4,Alice,103
EOF
```

Create `departments.csv`:

```bash
cat > departments.csv << EOF
101,Engineering
102,Marketing
103,Finance
EOF
```

Verify their contents:

```bash
cat employees.csv
```

Output:

```bash
1,John,101
2,Jane,102
3,Bob,101
4,Alice,103
```

```bash
cat departments.csv
```

Output:

```
CopyEdit
101,Engineering
102,Marketing
103,Finance
```

------

## Step 3: Upload Sample Datasets into HDFS

Next, we'll create dedicated directories in Hadoop’s distributed filesystem (HDFS) and upload the prepared datasets.

### WordCount Data

Create an HDFS directory and upload data:

```bash
hadoop fs -mkdir -p /user/root/wordcount/input
hadoop fs -put input.txt /user/root/wordcount/input/
```

Verify upload:

```bash
hadoop fs -ls /user/root/wordcount/input
```

------

### Temperature Data

Create HDFS directory and upload temperature data:

```bash
hadoop fs -mkdir -p /user/root/temperature/input
hadoop fs -put temperatures.csv /user/root/temperature/input/
```

Verify upload:

```bash
hadoop fs -ls /user/root/temperature/input
```

------

### Join Example Data

Create HDFS directories and upload join datasets:

```bash
hadoop fs -mkdir -p /user/root/join/input
hadoop fs -put employees.csv departments.csv /user/root/join/input/
```

Verify upload:

```bash
hadoop fs -ls /user/root/join/input
```

------

## Bonus Step: Using Built-in Hadoop MapReduce Examples

Hadoop provides several built-in MapReduce examples useful for quick testing and learning. Let's briefly run through two practical examples: **WordCount** and **Data Filtering (Grep)**, demonstrating Hadoop's built-in functionalities.

### Example 1: Built-in WordCount

This classic example counts occurrences of each word in a text file.

#### Run the built-in WordCount job:

```bash
# Execute the built-in WordCount example
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar wordcount \
    /user/root/wordcount/input \
    /user/root/wordcount/output

# Check the output results
hadoop fs -cat /user/root/wordcount/output/part-r-00000
```

**Explanation**:

- **Mapper**: Splits lines of text into words and emits `(word, 1)` pairs.
- **Reducer**: Receives grouped word occurrences and sums counts, producing `(word, total_count)`.

------

### Example 2: Built-in Data Filtering (Grep)

This built-in example filters lines matching a specific pattern—in our case, filtering lines containing the number "3".

#### Run the built-in Grep job:

```bash
# Run the built-in Grep example to filter lines containing "3"
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar grep \
    /user/root/temperature/input \
    /user/root/temperature/output \
    ".*3.*"

# View the output results
hadoop fs -cat /user/root/temperature/output/part-r-00000
```

**Explanation**:

- **Mapper**: Emits lines containing the specified regex pattern.
- **Reducer**: Consolidates matched results.

---



## Step 4: Verify Java Code and Scripts Availability

Your Docker Compose file maps your local Java examples (`./base/mapreduce_examples`) directly into your NameNode container at `/opt/mapreduce_examples`. To verify this, follow these steps:

1. **Navigate to the mapped directory**:

```bash
cd /opt/mapreduce_examples
```

2. **List the contents to confirm Java files and scripts exist**:

```bash
ls -la
```

You should see the following files already in place (from your repository):

- `CustomWordCount.java`
- `TemperatureFilter.java`
- `JoinExample.java`
- `compile.sh`
- `run.sh`

------

## Step 5: Review `compile.sh` and `run.sh` Scripts

These scripts should already be executable, but ensure they have execute permissions:

```bash
chmod +x compile.sh run.sh
```

Review the content quickly to confirm correctness:

```bash
cat compile.sh
cat run.sh
```

The scripts are used as follows:

- **`compile.sh`**: Compiles Java MapReduce examples into a runnable JAR file (`mapreduce-examples.jar`).
- **`run.sh`**: Executes compiled MapReduce examples (WordCount, TemperatureFilter, JoinExample) using the generated JAR.

------

## Final Checkpoint:

Your environment is now confirmed ready with:

- Pre-existing Java MapReduce examples available via Docker volumes at `/opt/mapreduce_examples`.
- Required datasets uploaded to HDFS.
- Scripts (`compile.sh` and `run.sh`) verified and executable.

Your preparation phase is complete and you're ready to proceed.

------

## Next Steps:

We can now continue to:

- **Section 4: Compiling Custom Java MapReduce Examples**

In this next section, you'll use the provided scripts to compile and package your MapReduce Java code into executable JARs, preparing for actual execution of MapReduce jobs.

---



# Section 4: Compiling Custom Java MapReduce Examples

In this section, you'll compile and package the provided Java MapReduce examples (`CustomWordCount.java`, `TemperatureFilter.java`, and `JoinExample.java`) into an executable JAR file using your Hadoop Docker environment.

These examples are already placed in your Docker container via your local repository, mounted at `/opt/mapreduce_examples`. We'll use the provided `compile.sh` script to automate compilation.

------

## Step 1: Access the NameNode Container Shell

First, access the Hadoop NameNode container to compile your Java examples.

```bash
docker exec -it namenode bash
```

Ensure you're in the correct directory inside the container:

```bash
cd /opt/mapreduce_examples
```

------

## Step 2: Verify Java Examples and Scripts

Before compilation, ensure all required Java files and scripts exist:

```bash
ls -la
```

Expected files are:

- `CustomWordCount.java`
- `TemperatureFilter.java`
- `JoinExample.java`
- `compile.sh`
- `run.sh`

If any file is missing, verify the local directory `./base/mapreduce_examples` in your local repository and restart Docker Compose if necessary.

------

## Step 3: Review `compile.sh` Script (Explanation)

Before running, briefly understand what the provided `compile.sh` script does:

```bash
cat compile.sh
```

**Explanation of `compile.sh`:**

- Creates required directories (`classes` and `jars`).
- Compiles Java files (`*.java`) using Hadoop's classpath.
- Packages compiled classes into a JAR file (`mapreduce-examples.jar`).

Here's the script for reference (already provided in your setup):

```bash
#!/bin/bash
mkdir -p classes
mkdir -p jars

# Compile all Java files
javac -classpath $(hadoop classpath) -d classes *.java

# Create a single JAR with all examples
jar -cvf jars/mapreduce-examples.jar -C classes .

echo "Compilation complete. JAR file created at jars/mapreduce-examples.jar"
```

------

## Step 4: Execute the Compilation Script

Set execute permissions (if not already done):

```bash
chmod +x compile.sh
```

Run the compilation script:

```bash
./compile.sh
```

You should see output similar to:

```bash
added manifest
adding: CustomWordCount.class(in = XXXX) (out= XXX bytes)(deflated XX%)
adding: CustomWordCount$TokenizerMapper.class(...)
adding: CustomWordCount$IntSumReducer.class(...)
adding: TemperatureFilter.class(...)
adding: TemperatureFilter$TempMapper.class(...)
adding: JoinExample.class(...)
adding: JoinExample$JoinMapper.class(...)
adding: JoinExample$JoinReducer.class(...)

Compilation complete. JAR file created at jars/mapreduce-examples.jar
```

------

## Step 5: Verify the JAR File Creation

Ensure the JAR file was created successfully:

```bash
ls -la jars/
```

You should see:

```
mapreduce-examples.jar
```

This JAR file contains all compiled MapReduce examples, ready for execution on Hadoop.

------

## Final Checkpoint:

By the end of this step, you should have:

- Successfully compiled all provided Java MapReduce examples.
- Created a single executable JAR file (`mapreduce-examples.jar`) in the `jars` folder.

Your MapReduce examples are now compiled and ready for running Hadoop jobs.

------

## Next Steps:

With the JAR file prepared, we will proceed to:

- **Section 5: Running Custom MapReduce Jobs (Hands-on Section)**

In the next section, you'll use the provided `run.sh` script to execute these compiled examples and view results directly from your Hadoop environment.

---



# Section 5: Running Custom MapReduce Jobs (Hands-on Section)

In this hands-on section, you'll execute the compiled MapReduce Java examples (`CustomWordCount`, `TemperatureFilter`, and `JoinExample`) on your Docker-based Hadoop environment. You'll use the provided `run.sh` script for executing each job and review the outputs directly from HDFS.

------

## Step 1: Ensure the Environment is Ready

Before running jobs, ensure you're inside the NameNode Docker container and in the correct directory:

```bash
docker exec -it namenode bash
cd /opt/mapreduce_examples
```

Confirm the JAR file (`mapreduce-examples.jar`) exists:

```bash
ls jars/mapreduce-examples.jar
```

You should see:

```
jars/mapreduce-examples.jar
```

------

## Step 2: Running the MapReduce Jobs

Use the provided `run.sh` script to execute each MapReduce job. The general syntax for running a job is:

```bash
./run.sh [wordcount|temperature|join] [output_directory]
```

Let's run each job step by step.

### Example 1: Custom WordCount Job

Run the Custom WordCount example:

```bash
./run.sh wordcount /user/root/wordcount/output-custom
```

**Explanation:**

- This executes the `CustomWordCount` Java class.
- Input data (`input.txt`) is read from `/user/root/wordcount/input`.
- Output results will be stored in `/user/root/wordcount/output-custom`.

After execution completes, you'll see a message:

```
Job completed. Results:
```

Check the detailed job output by manually running:

```bash
hadoop fs -cat /user/root/wordcount/output-custom/part-*
```

**Expected Output (example):**

```
hadoop	1
hello	1
implements	1
is	2
mapreduce	3
model	1
programming	1
sample	1
text	1
this	1
to	1
welcome	1
world	1
```

------

### Example 2: TemperatureFilter Job

Run the TemperatureFilter example:

```bash
./run.sh temperature /user/root/temperature/output-custom
```

**Explanation:**

- Executes the `TemperatureFilter` class.
- Reads input from `/user/root/temperature/input`.
- Filters temperature records greater than 30°F and stores results at `/user/root/temperature/output-custom`.

Check the detailed job output:

```bash
hadoop fs -cat /user/root/temperature/output-custom/part-*
```

**Expected Output:**

```
station1,2021-01-01,32.5
station1,2021-01-02,33.1
station3,2021-01-01,35.6
station3,2021-01-02,36.2
```

------

### Example 3: JoinExample Job

Run the JoinExample:

```bash
./run.sh join /user/root/join/output-custom
```

**Explanation:**

- Executes the `JoinExample` Java class.
- Reads employee and department datasets from `/user/root/join/input`.
- Joins datasets on department ID and outputs joined results at `/user/root/join/output-custom`.

Check the detailed job output:

```bash
hadoop fs -cat /user/root/join/output-custom/part-*
```

**Expected Output:**

```
101	1,John,Engineering
101	3,Bob,Engineering
102	2,Jane,Marketing
103	4,Alice,Finance
```

------

## Step 3: Troubleshooting Common Issues

During execution, you might face common issues. Here are quick troubleshooting tips:

- **Output directory already exists**:

  - Remove the existing output directory before rerunning the job:

    ```bash
    hadoop fs -rm -r /user/root/[job_name]/output-custom
    ```

- **Permission errors in HDFS**:

  - Ensure proper permissions for HDFS directories.

- **Incorrect Java Class Errors**:

  - Verify the Java file names, class names, and compilation steps.
  - Confirm the JAR file includes all compiled classes.

------

## Final Checkpoint:

After completing this section, you've successfully:

- Run three distinct MapReduce examples (WordCount, TemperatureFilter, and JoinExample).
- Checked job outputs directly from HDFS.
- Learned basic troubleshooting and job-monitoring via Hadoop UIs.

------

## Next Steps:

In the next section, you'll explore advanced concepts to enhance your MapReduce skills:

- **Section 6: Advanced Concepts in MapReduce**
  - Shuffle/sort optimization techniques.
  - Understanding Input/Output formats and serialization.
  - Implementing a custom partitioner for efficient data processing.

This section will further enhance your understanding and capability with MapReduce programming.

---



# Section 6: Advanced Concepts in MapReduce (Optional)

Now that you've successfully executed basic MapReduce jobs, it’s time to explore advanced MapReduce concepts. This section covers optimization techniques, input/output formats, serialization, and custom partitioners, giving you deeper insights into efficient and effective MapReduce programming.

------

## 6.1 Shuffle and Sort Phase Optimization Techniques

The shuffle and sort phase in MapReduce is critical for performance, especially when processing large-scale datasets. Optimizing this phase can significantly reduce runtime and resource usage.

### Understanding Shuffle and Sort

This phase occurs automatically after the map tasks and involves:

- **Shuffle**: Transferring intermediate mapper outputs to the reducers.
- **Sort**: Sorting intermediate data based on keys before passing to reducers.

This phase is resource-intensive due to heavy network and I/O usage.

### Optimization Techniques

**1. Using Combiners**

- Combiners locally aggregate mapper outputs, reducing data transferred across the network.
- They usually implement similar logic to reducers.

**Combiner Pseudocode (WordCount):**

```pseudo
function Combiner(intermediate_key, list_of_values):
    partial_sum = sum(list_of_values)
    emit(intermediate_key, partial_sum)
```

**2. Adjusting Shuffle Configuration**
 Hadoop configuration properties that can optimize shuffle/sort include:

- `mapreduce.task.io.sort.mb`:
  Increases buffer size used for sorting mapper outputs, enhancing sort performance.
- `mapreduce.reduce.shuffle.parallelcopies`:
  Controls the number of parallel data fetches by reducers, affecting network utilization.
- `mapreduce.job.reduces`:
  Increasing the number of reducers helps distribute workload evenly, improving parallelism.

------

## 6.2 Input and Output Formats in MapReduce

Input and output formats define how data is read into MapReduce jobs and how the output is stored in HDFS.

### Common Input Formats:

- **TextInputFormat** (default):
  - Splits input files line by line, keys are byte offsets, values are lines.
- **KeyValueTextInputFormat**:
  - Splits each line into key-value pairs, separated by tabs.
- **SequenceFileInputFormat**:
  - Binary format for efficient data storage and faster processing at large scales.

### Common Output Formats:

- **TextOutputFormat** (default):
  - Writes output in plain text, key-value pairs separated by tabs.
- **SequenceFileOutputFormat**:
  - Efficient binary output suitable for subsequent MapReduce jobs.

### Configuring Input/Output Formats in Java Job:

```java
// Set Input Format
job.setInputFormatClass(TextInputFormat.class);

// Set Output Format
job.setOutputFormatClass(TextOutputFormat.class);
```

------

## 6.3 Serialization in MapReduce (Writable Classes)

Serialization converts data objects into byte streams for efficient storage and transmission. Hadoop provides specialized interfaces for serialization:

- **Writable Interface**: Implemented by data types used in Hadoop.
- **WritableComparable Interface**: Extends Writable, adding methods for sorting.

### Common Writable Classes:

- `Text`: Represents strings.
- `IntWritable`: Represents integers.
- `LongWritable`: Represents long integers.
- `FloatWritable`: Represents floating-point numbers.

### Example: Custom Writable Class (Advanced)

Define a custom Writable if standard types don't meet your needs.

```java
import org.apache.hadoop.io.*;

public class EmployeeWritable implements Writable {
    private Text name;
    private IntWritable salary;

    // Constructor
    public EmployeeWritable() {
        this.name = new Text();
        this.salary = new IntWritable();
    }

    // Write data
    public void write(DataOutput out) throws IOException {
        name.write(out);
        salary.write(out);
    }

    // Read data
    public void readFields(DataInput in) throws IOException {
        name.readFields(in);
        salary.readFields(in);
    }

    // Setters, getters, and other methods
}
```

------

## 6.4 Implementing a Custom Partitioner

Partitioners determine which reducer receives each mapper’s intermediate key-value pair, helping balance load across reducers and enhancing efficiency.

### Default Partitioner:

- Hashes key values to determine reducers (`HashPartitioner`).

### Custom Partitioner:

Create a custom partitioner to explicitly control data distribution.

#### Example Scenario:

Partition data based on temperature stations (station-based partitioner):

**Custom Partitioner Java Example:**

```java
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class StationPartitioner extends Partitioner<Text, Text> {

    @Override
    public int getPartition(Text key, Text value, int numReducers) {
        // Assume station IDs are numeric, e.g., station1, station2...
        String stationId = key.toString().replaceAll("\\D+", "");
        int stationNum = Integer.parseInt(stationId);

        // Ensure non-negative partitions
        return (stationNum % numReducers);
    }
}
```

### Using Custom Partitioner in Your Job:

Configure it within your MapReduce Java code:

```java
job.setPartitionerClass(StationPartitioner.class);
job.setNumReduceTasks(3);  // Example: 3 reducers
```

------

## 6.5 Demonstrating Custom Partitioner (Hands-on):

If you wish to apply your partitioner practically:

1. **Compile** your Java files including `StationPartitioner.java`:

```bash
javac -classpath $(hadoop classpath) -d classes *.java
jar -cvf jars/mapreduce-examples.jar -C classes .
```

1. **Run** your job specifying the custom partitioner:

- Adjust your `run.sh` script or execute manually:

```bash
hadoop jar jars/mapreduce-examples.jar TemperatureFilter \
    -D mapreduce.job.reduces=3 \
    /user/root/temperature/input \
    /user/root/temperature/output-partitioner
```

This will partition output based on your custom logic.

------

## 6.6 Best Practices and Recommendations:

- **Use Combiners:** Whenever your reducer is associative and commutative (e.g., sum, count), use combiners.
- **Appropriate Partitioners:** Ensure keys distribute evenly across reducers to avoid hotspots.
- **Optimize Configuration:** Adjust Hadoop configuration parameters based on data size and job complexity.
- **Monitor Jobs:** Regularly monitor resource usage and adjust configurations as necessary through Hadoop Web UIs.

------

## Final Checkpoint:

At the end of this advanced concepts section, you should have a clear understanding of:

- Shuffle and sort optimizations (combiners, partitioners, configurations).
- Hadoop input/output formats and their efficient usage.
- Hadoop’s serialization mechanisms (Writable classes).
- Writing and applying a custom partitioner to distribute data effectively across reducers.

These skills enable you to optimize and tailor MapReduce jobs for real-world large-scale data processing.

------

## Conclusion

Throughout this comprehensive tutorial, you have learned how to:

- Set up a Hadoop environment using Docker.
- Understand and articulate fundamental MapReduce concepts and workflow.
- Prepare and manage datasets within Hadoop's filesystem (HDFS).
- Compile and run custom Java MapReduce programs.
- Execute Hadoop’s built-in MapReduce examples for quick testing.

By gaining hands-on experience with practical examples and real-world scenarios, you are now well-equipped to use MapReduce effectively to process large datasets. Continue experimenting and exploring additional Hadoop tools and resources to deepen your Big Data expertise.