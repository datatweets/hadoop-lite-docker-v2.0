# Understanding and Using HDFS and YARN on Hadoop (Docker-based Environment)

In this tutorial, you'll explore two critical components of Hadoop: **Hadoop Distributed File System (HDFS)** and **Yet Another Resource Negotiator (YARN)**. You'll understand key concepts, execute hands-on exercises, and gain practical experience managing your Hadoop environment.

---

## Tutorial Outline

* Section 1: Introduction to HDFS and YARN
* Section 2: Hands-on with HDFS
* Section 3: Hands-on with YARN
* Section 4: Monitoring and Administration Tips
* Section 5: Conclusion and Best Practices

---

## Section 1: Introduction to HDFS and YARN

### Hadoop Distributed File System (HDFS)

**HDFS** is a scalable, fault-tolerant filesystem designed for handling large datasets distributed across commodity hardware.

**Core Components**:

* **NameNode**:

  * Central coordinator.
  * Stores metadata (file names, directory structure, locations of blocks).

* **DataNodes**:

  * Store actual data blocks.
  * Manage data storage, replication, and integrity.

**Key Features**:

* Data replication for fault tolerance (default replication factor is 3).
* Optimized for large, sequential data reads and writes.
* Designed for scalability and reliability.

### Yet Another Resource Negotiator (YARN)

**YARN** manages resources within a Hadoop cluster, ensuring efficient and balanced use of cluster resources.

**Core Components**:

* **ResourceManager**:

  * Allocates resources (CPU, memory) to running applications.
  * Manages overall resource usage across cluster.

* **NodeManager**:

  * Runs on each cluster node.
  * Manages resource usage on its node.
  * Reports resource availability to ResourceManager.

* **ApplicationMaster**:

  * Negotiates resources from ResourceManager.
  * Manages task execution within an application.

---

## Section 2: Hands-on with HDFS

### Step 1: Access Hadoop NameNode Shell

Enter your Hadoop NameNode container:

```bash
docker exec -it namenode bash
```

### Step 2: Basic HDFS Commands

* **List root directory** in HDFS:

```bash
hadoop fs -ls /
```

* **Create HDFS directories**:

```bash
hadoop fs -mkdir -p /user/hadoop/sample_dir
```

* **List created directories**:

```bash
hadoop fs -ls /user/hadoop
```

### Step 3: Uploading Files to HDFS

* **Create a local sample file** within your container:

```bash
echo "This is a sample file for HDFS." > sample_file.txt
```

* **Upload this local file to HDFS**:

```bash
hadoop fs -put sample_file.txt /user/hadoop/sample_dir/
```

* **Verify file upload**:

```bash
hadoop fs -ls /user/hadoop/sample_dir
```

### Step 4: Reading Files from HDFS

* **Read file contents directly from HDFS**:

```bash
hadoop fs -cat /user/hadoop/sample_dir/sample_file.txt
```

### Step 5: HDFS File Operations

* **Copy file within HDFS**:

```bash
hadoop fs -cp /user/hadoop/sample_dir/sample_file.txt /user/hadoop/sample_dir/sample_file_copy.txt
```

* **Move or rename a file within HDFS**:

```bash
hadoop fs -mv /user/hadoop/sample_dir/sample_file_copy.txt /user/hadoop/sample_dir/sample_file_renamed.txt
```

* **Remove file from HDFS**:

```bash
hadoop fs -rm /user/hadoop/sample_dir/sample_file_renamed.txt
```

---

## Section 3: Hands-on with YARN

In this section, you'll interact with YARN without running a MapReduce job, demonstrating management capabilities provided by YARN.

### Step 1: Check YARN ResourceManager UI

* Open ResourceManager web UI in your browser:

```
http://localhost:8088
```

* You’ll see cluster details, resource usage, and running applications.

### Step 2: Listing YARN Applications (CLI)

* **List all applications** (running, finished, failed):

```bash
yarn application -list -appStates ALL
```

* **List only running applications**:

```bash
yarn application -list
```

### Step 3: Detailed Application Status and Logs

If you have any application ID (from previous list):

```bash
yarn application -status <application_id>
```

**Example**:

```bash
yarn application -status application_1716023432159_0001
```

* **View application logs**:

```bash
yarn logs -applicationId <application_id>
```

### Step 4: Inspecting YARN Cluster Nodes

* **Check node statuses** within your YARN cluster:

```bash
yarn node -list -all
```

This lists all cluster nodes, their state (RUNNING, LOST), and resource usage.

---

## Section 4: Monitoring and Administration Tips

### Important HDFS Web Interfaces:

* **NameNode UI** (for HDFS management):

```
http://localhost:50070
```

### Important YARN Web Interfaces:

* **ResourceManager UI** (for resource monitoring and application tracking):

```
http://localhost:8088
```

* **JobHistory Server UI** (completed job histories, useful even without running jobs):

```
http://localhost:19888
```

### Troubleshooting Common Issues:

* **HDFS permission errors**:

```bash
hadoop fs -chmod -R 755 /user/hadoop/sample_dir
```

* **NodeManager not running**: Check container logs or restart container via docker-compose:

```bash
docker logs nodemanager
docker restart nodemanager
```

---

## Section 5: Conclusion and Best Practices

### Key Takeaways:

* **HDFS** efficiently stores and manages large-scale distributed data.
* **YARN** efficiently manages and allocates cluster resources to applications.
* CLI and Web UI tools provide extensive control and monitoring capabilities.

### Best Practices:

* Regularly check Hadoop Web UIs to monitor system health.
* Structure your HDFS data clearly, using meaningful directory structures.
* Monitor YARN regularly to optimize resource allocation and troubleshoot efficiently.
* Always test file operations with small datasets before large-scale tasks.

### Recommended Further Resources:

* [Official Hadoop Documentation](https://hadoop.apache.org/docs/current/)
* ["Hadoop: The Definitive Guide" by Tom White](https://www.oreilly.com/library/view/hadoop-the-definitive/9781491901687/)
* Hadoop Administration online tutorials and courses.
