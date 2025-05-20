# Hive Data Warehousing: Step-by-Step Tutorial

## Outline

1. **Introduction to Hive and Data Warehousing**
   - What is Hive?
   - Hive architecture (with your Docker stack)
   - HiveQL and why it matters
2. **HiveQL vs ANSI SQL Syntax**
   - Key similarities and differences
   - Common pitfalls and workarounds
   - Demo queries
3. **Partitioning in Hive**
   - Why partition?
   - Static vs Dynamic partitioning: concepts, pros/cons
   - Step-by-step: create partitioned tables
4. **Hive File Formats: ORC vs Parquet**
   - What are they? Why do they matter?
   - Table creation with ORC/Parquet
   - Performance notes
5. **Hands-on: Analyzing a Large Dataset**
   - Uploading data to HDFS
   - Creating and loading tables (external/raw, partitioned)
   - Dynamic partitioning demo
   - Query performance showcase
6. **Conclusion and Best Practices**
   - Recap of key lessons
   - Common issues & troubleshooting
   - Optimization tips

------

------

## Section 1: Introduction to Hive and Data Warehousing

### 1.1 What is Hive?

Apache Hive is a data warehouse system built on top of Hadoop. It lets you analyze large datasets stored in HDFS using a SQL-like language called **HiveQL**. Instead of writing complex MapReduce jobs, you write familiar queries, and Hive translates them into efficient jobs for distributed processing.

**Key Features:**

- **SQL-like interface:** Lowers the barrier for analysts and engineers.
- **Integration with Hadoop:** Runs on HDFS, uses MapReduce, Tez, or Spark as execution engines.
- **Metastore:** Centralized schema and metadata management.

#### Pseudocode Analogy

Traditional approach:

```
Write Java MapReduce → Compile → Run → Parse Output
```

Hive approach:

```
Write SQL-like query (HiveQL) → Hive parses/optimizes → Hive generates MapReduce/Tez/Spark jobs → Get result
```

This means you can focus on *what* you want (queries), not *how* it’s done in the backend.

------

### 1.2 Hive Architecture (with Your Docker Stack)

Your environment contains:

- **Hive Server**: Receives HiveQL, coordinates execution.
- **Metastore (PostgreSQL)**: Stores table schemas, partition info, etc.
- **Hue**: GUI for query and table management.
- **HDFS (Namenode/Datanode)**: Storage layer for raw and managed data.

**Typical Workflow:**

1. User submits HiveQL query via Hue (web) or Beeline (CLI).
2. Hive Server parses the query, consults Metastore for table details.
3. Query is compiled to execution jobs (MapReduce, Tez, or Spark).
4. Results are read/written to HDFS, metadata updated in Metastore.
5. Results shown in Hue or CLI.

**Diagram:** (Textual, for now)

```
[Hue/Beeline] ---> [HiveServer] ---> [Metastore (PostgreSQL)]
          |               |                  ^
          v               v                  |
      [HDFS: Namenode/Datanode] <------------
```

------

### 1.3 How to Connect to Hive in Your Docker Setup

You can use either **Hue** (recommended for beginners) or **Beeline CLI** for direct commands.

**a) Using Hue:**
 Open your browser to `http://localhost:8888` (or whatever port is mapped), login to Hue, and choose the Hive or SQL Editor.

**b) Using Beeline (from Docker container):**

```bash
docker exec -it hive-server bash
beeline -u jdbc:hive2://localhost:10000
```

- Use `!connect` if asked for more connection details (defaults often suffice).
- All SQL commands can be run from this shell.

------

### 1.4 Example: Your First Hive Query

Let’s make sure your stack is running:

```bash
docker compose ps
```

You should see all relevant services (`hive-server`, `hue`, `metastore`, `namenode`, `datanode`, etc.) **healthy**.

**Run a test query:**
 In Hue or Beeline, try:

```sql
SHOW DATABASES;
```

You should see at least `default` and `information_schema`.

------

### 1.5 Practical Checkpoint

#### If you have sample data loaded (or sample tables), try:

```sql
CREATE TABLE IF NOT EXISTS test_table (id INT, value STRING);
INSERT INTO test_table VALUES (1, 'foo'), (2, 'bar');
SELECT * FROM test_table;
```

**Expected result:** You’ll see your sample rows.

------

#### **Recap for Section 1**

- Hive is a bridge from SQL to Big Data.
- Your stack is ready for both web-based (Hue) and command-line (Beeline) access.
- All interaction with data is through HiveQL, mapped to distributed processing.

---

---

## Section 2: HiveQL vs. ANSI SQL Syntax

### 2.1 What Is HiveQL?

**HiveQL** (Hive Query Language) is the SQL dialect used by Apache Hive.
 It is designed to be familiar to anyone with SQL experience but has limitations and extensions due to its Big Data context and Hadoop integration.

#### Why is HiveQL different from ANSI SQL?

- HiveQL targets **batch, distributed processing** rather than interactive OLTP databases.
- Some operations (like UPDATE, DELETE) are limited or require special settings.
- Certain SQL features are not supported because they don’t fit Hadoop’s write-once, read-many storage model.

------

### 2.2 Data Types: What’s Supported and What’s Not

| **Category**  | **ANSI SQL Example**  | **HiveQL Example**       | **Supported in Hive?** | **Notes**                                                    |
| ------------- | --------------------- | ------------------------ | ---------------------- | ------------------------------------------------------------ |
| Numbers       | `INTEGER`, `FLOAT`    | `INT`, `BIGINT`, `FLOAT` | Yes                    | Some types missing (e.g., `TINYINT`, `SMALLINT` in newer Hive) |
| Strings       | `CHAR`, `VARCHAR`     | `STRING`, `VARCHAR`      | Yes                    | `STRING` = unlimited length                                  |
| Dates & Times | `DATE`, `TIMESTAMP`   | `DATE`, `TIMESTAMP`      | Yes                    | `DATETIME` not supported                                     |
| Complex Types | `ARRAY`, `MAP`, `ROW` | `ARRAY`, `MAP`, `STRUCT` | Yes                    | Not standard in ANSI SQL                                     |
| Boolean       | `BOOLEAN`             | `BOOLEAN`                | Yes (newer Hive)       |                                                              |

#### Example Table Definition (HiveQL):

```sql
CREATE TABLE customers (
  id INT,
  name STRING,
  signup_date DATE,
  tags ARRAY<STRING>
);
```

------

### 2.3 Table Creation and Loading: Differences

**ANSI SQL** usually separates schema from storage, and can use `CREATE TABLE ... AS SELECT ...` (CTAS) or `INSERT INTO ... VALUES`.

**HiveQL** supports CTAS and external/managed tables, but initial data loading is almost always from HDFS/local files, not inline values.

#### Example:

**ANSI SQL (e.g., MySQL/Postgres):**

```sql
INSERT INTO employees (id, name, salary) VALUES (1, 'Amin', 5000);
```

**HiveQL:**

```sql
-- Not recommended to insert row-by-row. Best to load files or use SELECT
LOAD DATA INPATH '/user/hive/warehouse/employees.csv' INTO TABLE employees;
```

------

### 2.4 Query Syntax: Key Differences & Examples

Let’s compare common operations side by side.

#### a) LIMIT and FETCH

- ANSI SQL:

  ```sql
  SELECT * FROM employees FETCH FIRST 10 ROWS ONLY;
  ```

- HiveQL:

  ```sql
  SELECT * FROM employees LIMIT 10;
  ```

#### b) String Functions

- ANSI SQL:

  ```sql
  SELECT UPPER(name) FROM employees;
  ```

- HiveQL:

  ```sql
  SELECT upper(name) FROM employees;
  ```

#### c) Aggregations

- ANSI SQL:

  ```sql
  SELECT department, AVG(salary) FROM employees GROUP BY department;
  ```

- HiveQL:

  ```sql
  SELECT department, avg(salary) FROM employees GROUP BY department;
  ```

#### d) UPDATE/DELETE (Very Different!)

- ANSI SQL (supported by default):

  ```sql
  UPDATE employees SET salary = salary * 1.1 WHERE department = 'IT';
  DELETE FROM employees WHERE id = 1;
  ```

- HiveQL (limited support, ACID tables only!):

  ```sql
  -- Only on transactional tables with special settings
  UPDATE employees SET salary = salary * 1.1 WHERE department = 'IT';
  DELETE FROM employees WHERE id = 1;
  ```

  > **Note:** By default, most Hive tables are not transactional. Enabling this slows performance and requires special configuration (not recommended for bulk, append-only data).

#### e) INSERT

- ANSI SQL:

  ```sql
  INSERT INTO employees VALUES (1, 'Ali', 3000);
  ```

- HiveQL:

  ```sql
  -- Inline VALUES is supported but rarely used. More common:
  INSERT INTO employees SELECT * FROM new_employees;
  ```

------

### 2.5 Joins and Subqueries

**Joins:** Both ANSI SQL and HiveQL support INNER, LEFT, RIGHT, and FULL joins. However, Hive’s join performance depends on data distribution, partitioning, and execution engine.

```sql
SELECT a.id, a.name, b.dept_name
FROM employees a
JOIN departments b
  ON a.dept_id = b.id;
```

**Subqueries:** Supported, but can be slower due to distributed execution.

------

### 2.6 Unsupported/Partially Supported Features

| Feature                 | ANSI SQL | HiveQL Support     | Alternative/Comment            |
| ----------------------- | -------- | ------------------ | ------------------------------ |
| Foreign Keys            | Yes      | No                 | Use joins instead              |
| Indexes                 | Yes      | No (basic support) | Rely on partitioning/bucketing |
| Transactions            | Yes      | Limited (ACID)     | Only for transactional tables  |
| Constraints (PK/UNIQUE) | Yes      | No                 | Responsibility on ETL process  |

------

### 2.7 Practical Hands-On: Try These Out!

#### Step 1: Create a Table (HiveQL)

```sql
CREATE TABLE employees (
  id INT,
  name STRING,
  salary DOUBLE,
  department STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
STORED AS TEXTFILE;
```

*Run in Hue or Beeline.*

#### Step 2: Insert Data (LOAD DATA recommended)

First, prepare a CSV file (`employees.csv`) with these contents:

```
1,Ali,3500,IT
2,Mina,4200,HR
3,John,4100,Finance
```

Upload it to HDFS:

```bash
docker cp /datasets/employees.csv namenode:/tmp/employees.csv
docker exec -it namenode bash
hdfs dfs -mkdir -p /user/hive/warehouse/
hdfs dfs -put /tmp/employees.csv /user/hive/warehouse/
```

Then, in Hive:

```sql
LOAD DATA INPATH 'hdfs://namenode:8020/user/hive/warehouse/employees.csv' INTO TABLE employees;
```

#### Step 3: Query Table

```sql
SELECT * FROM employees LIMIT 5;
SELECT department, AVG(salary) FROM employees GROUP BY department;
```

#### Step 4: Try a Join

Suppose you create a departments table and load it similarly; then:

```sql
SELECT e.name, d.department_name
FROM employees e
JOIN departments d ON (e.department = d.department_id);
```

------

### 2.8 Summary Table: HiveQL vs. ANSI SQL

| Feature             | HiveQL                              | ANSI SQL           |
| ------------------- | ----------------------------------- | ------------------ |
| Data Types          | Basic, Hadoop-optimized             | Rich, full SQL     |
| Row-by-row INSERT   | Not recommended; use LOAD or SELECT | Supported          |
| UPDATE/DELETE       | Transactional tables only           | Supported          |
| Partitioning        | Native, essential for performance   | Not always present |
| Indexes/Constraints | Not enforced                        | Enforced           |

------

## **Key Teaching Points**

- HiveQL is familiar, but differences affect ETL and analytics workflows.
- Always use **LOAD DATA** or **SELECT ... INSERT** for big data. Avoid row-by-row inserts.
- For complex analytics, **partitioning** and **file format** choices have bigger impact in Hive than SQL DBs.
- UPDATE/DELETE is special and rarely used for Big Data workloads.

------

---

# Section 3: Partitioning in Hive (Static vs. Dynamic)

## 3.1 Why Partitioning Matters

**Partitioning** is one of the most powerful features in Hive for optimizing queries on large datasets. It divides a table into smaller, more manageable parts based on column values (e.g., date, region), so queries can “prune” irrelevant partitions and read only what’s needed.

- **Example:** If your sales table is partitioned by year and month, a query for January 2024 only scans that month—not the entire table.
- **Result:** Huge improvements in speed and resource efficiency.

### Analogy

Think of partitioning like a well-organized filing cabinet:
 If each drawer is a year, and folders inside are months, you can instantly find all receipts from Jan 2024 without digging through the entire cabinet.

------

## 3.2 Types of Partitioning in Hive

**A. Static Partitioning:**
 You *explicitly* specify partition values when loading data.

- Best when partition values are fixed and known ahead of time.

**B. Dynamic Partitioning:**
 Hive *automatically* determines partition values based on the data.

- Essential when loading data with many different partition keys.

------

## 3.3 Table Design for Partitioning

Suppose we have a raw sales CSV like this:

```
transaction_id,customer_id,amount,region,year,month,day
1001,C01,100.5,North,2024,1,5
1002,C02,250.0,East,2024,1,6
...
```

**Partition Strategy:** Partition by `year` and `month` (common for time-based data).

------

## 3.4 Creating a Partitioned Table in Hive

### Step 1: Create an External Raw Table (No Partitions)

First, register your raw CSV in Hive (external table, no partitions):

```sql
CREATE EXTERNAL TABLE sales_raw (
  transaction_id STRING,
  customer_id    STRING,
  amount         FLOAT,
  region         STRING,
  year           INT,
  month          INT,
  day            INT
)
ROW FORMAT DELIMITED
  FIELDS TERMINATED BY ','
STORED AS TEXTFILE
LOCATION 'hdfs://namenode:8020/user/hive/warehouse/';
```

**Explanation:**

- Use `EXTERNAL` to avoid Hive moving/deleting your original data.
- Data is read directly from files in HDFS.

------

### Step 2: Create a Partitioned Table (Managed or External)

```sql
CREATE TABLE sales_partitioned (
  transaction_id STRING,
  customer_id    STRING,
  amount         FLOAT,
  region         STRING,
  day            INT
)
PARTITIONED BY (
  year INT,
  month INT
)
STORED AS ORC;
```

**Explanation:**

- The partition columns (`year`, `month`) are separated in the metadata; data is stored in subdirectories.

------

## 3.5 Static Partitioning: Load Data into a Specific Partition

**Scenario:** You want to load all January 2024 data.

### Step 1: Filter the source data and load it into a specific partition

```sql
INSERT INTO TABLE sales_partitioned PARTITION (year=2024, month=1)
SELECT
  transaction_id,
  customer_id,
  amount,
  region,
  day
FROM sales_raw
WHERE year=2024 AND month=1;
```

**How it works:**

- You specify which partition the data will go into.
- All rows in the SELECT must match the partition values (`2024, 1`).

**Directory structure on HDFS:**

```
/user/hive/warehouse/sales_partitioned/year=2024/month=1/
```

------

## 3.6 Dynamic Partitioning: Let Hive Determine Partition Values

**Scenario:** You want to load all available data, and let Hive split the data by year and month.

### Step 1: Enable Dynamic Partitioning

```sql
SET hive.exec.dynamic.partition = true;
SET hive.exec.dynamic.partition.mode = nonstrict;
```

**Explanation:**

- By default, Hive requires at least one static partition column.
- `nonstrict` mode allows all partition columns to be dynamic.

### Step 2: Insert with Dynamic Partitioning

```sql
INSERT INTO TABLE sales_partitioned PARTITION (year, month)
SELECT
  transaction_id,
  customer_id,
  amount,
  region,
  day,
  year,
  month
FROM sales_raw;
```

**How it works:**

- Hive scans all rows, and creates a partition for each unique `(year, month)` combination found in the data.
- Subdirectories are created automatically.

**Check the new partitions:**

```sql
SHOW PARTITIONS sales_partitioned;
```

------

## 3.7 Visualizing Partitioned Data on HDFS

Partitioned data will be stored like this:

```
/user/hive/warehouse/sales_partitioned/year=2024/month=1/
                                               /month=2/
                                /year=2023/month=12/
```

- Only relevant partitions are scanned for queries with `WHERE year=2024 AND month=1`.

------

## 3.8 Best Practices and Troubleshooting

- **Choose partition columns wisely:**
   Time columns (`year`, `month`, `day`) or low-cardinality attributes (e.g., `region`).
   *Do not partition on high-cardinality columns (e.g., user_id) or you’ll end up with too many partitions!*

- **List all partitions after dynamic load:**

  ```sql
  MSCK REPAIR TABLE sales_partitioned;
  ```

  (Especially important if you load data outside Hive, e.g., directly in HDFS.)

- **Partition Pruning:**
   When you run:

  ```sql
  SELECT * FROM sales_partitioned WHERE year=2024 AND month=1;
  ```

  Hive *prunes* partitions—reads only those relevant directories.

- **Dynamic partition limits:**
   The default Hive limit for dynamic partitions per insert is 100.
   Increase if needed:

  ```sql
  SET hive.exec.max.dynamic.partitions = 1000;
  ```

------

## 3.9 Practice: Full Demo for Your Students

Here’s a quick, repeatable process using your Docker-based Hive stack:

**Assume your raw CSV is already uploaded to `/user/hive/raw_data/sales.csv`.**

**1. Register raw table:**

```sql
CREATE EXTERNAL TABLE sales_raw (
  transaction_id STRING,
  customer_id STRING,
  amount FLOAT,
  region STRING,
  year INT,
  month INT,
  day INT
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','

LOCATION 'hdfs://namenode:8020/user/hive/warehouse/sales_raw_data/'
TBLPROPERTIES ("skip.header.line.count"="1");
```

**2. Create partitioned table:**

```sql
CREATE TABLE sales_partitioned (
  transaction_id STRING,
  customer_id STRING,
  amount FLOAT,
  region STRING,
  day INT
)
PARTITIONED BY (year INT, month INT)
STORED AS ORC
LOCATION 'hdfs://namenode:8020/user/hive/warehouse/sales_partitioned_data/';
```

**3. Enable dynamic partitioning:**

```sql
-- Set properties for dynamic partitioning
SET hive.exec.dynamic.partition = true;
SET hive.exec.dynamic.partition.mode = nonstrict;

-- Additional settings that might help
SET hive.exec.compress.output=true;
SET hive.exec.orc.default.compress=SNAPPY;
```

**4. Insert all data with dynamic partitioning:**

```sql
-- Insert data. OVERWRITE is optional.
INSERT OVERWRITE TABLE sales_partitioned PARTITION (year, month)
SELECT transaction_id, customer_id, amount, region, day, year, month
FROM sales_raw;
```

**5. Show partitions:**

```sql
SHOW PARTITIONS sales_partitioned;
```

**6. Query partitioned data (partition pruning in action):**

```sql
SELECT region, SUM(amount) AS total_sales
FROM sales_partitioned
WHERE year=2024 AND month=1
GROUP BY region;
```

------

## 3.10 Summary: Key Teaching Points

- Partitioning is essential for scalable analytics in Hive.
- **Static partitioning**: Specify partition in each load—good for fixed/batch processes.
- **Dynamic partitioning**: Hive detects and manages partitions automatically—best for big, diverse datasets.
- Always monitor the number and structure of partitions for optimal performance.

---

---

# Section 4: Hive File Formats—ORC vs. Parquet

## 4.1 What Are Hive File Formats?

Hive tables can store data in several formats. The two most popular **columnar storage** formats are:

- **ORC** (Optimized Row Columnar): Designed specifically for Hive, with deep compression and indexing.
- **Parquet**: Popular open format, used across Hadoop ecosystem—works well with Spark, Impala, etc.

**Why Columnar?**

- Faster for analytical queries: Reads only required columns.
- Better compression: Similar column values compress efficiently.
- Optimized for aggregate/reporting workloads (most data warehouse queries).

------

## 4.2 ORC Format in Hive

**ORC** is highly efficient for Hive. It supports lightweight storage, fast read/write, and includes features like:

- Built-in compression (ZLIB/SNAPPY)
- Predicate pushdown (query only what you need)
- Lightweight indexes inside each file

**When to use ORC?**

- When your analytics workload is mostly Hive.
- You need the best storage and query efficiency in Hive.

------

## 4.3 Parquet Format in Hive

**Parquet** is an open-source, Hadoop-wide columnar storage format.

- Supported by Hive, Spark, Impala, Drill, Presto, and many more tools.
- Efficient for read/write and cross-platform analytics.
- Great for mixed-stack environments.

**When to use Parquet?**

- If you plan to use the data with Spark, Impala, Presto, or Python tools.
- If your organization values interoperability.

------

## 4.4 Creating Tables with ORC and Parquet: Step-by-Step

### A. Creating an ORC Table

#### In Beeline/Hue:

```sql
CREATE TABLE employees_orc (
  id INT,
  name STRING,
  salary DOUBLE,
  department STRING
)
STORED AS ORC;
```

**Explanation:**

- `STORED AS ORC` sets the storage format.
- Data will be stored as ORC files under `/user/hive/warehouse/employees_orc/` on HDFS.

#### From the Command Line (Docker shell):

```bash
docker exec -it hive-server bash
beeline -u jdbc:hive2://localhost:10000

-- Now in Beeline shell:
CREATE TABLE employees_orc (
  id INT,
  name STRING,
  salary DOUBLE,
  department STRING
)
STORED AS ORC;
```

------

### B. Creating a Parquet Table

```sql
CREATE TABLE employees_parquet (
  id INT,
  name STRING,
  salary DOUBLE,
  department STRING
)
STORED AS PARQUET;
```

*The process is the same: just replace `ORC` with `PARQUET`.*

------

## 4.5 Loading Data into ORC/Parquet Tables

### Method 1: Insert-Select (Recommended for Format Conversion)

Suppose you have an existing text-based table (`employees_raw`). You want to convert the data into ORC or Parquet.

```sql
-- Insert into ORC table from text table
INSERT INTO employees_orc
SELECT * FROM employees_raw;

-- Insert into Parquet table from text table
INSERT INTO employees_parquet
SELECT * FROM employees_raw;
```

**Explanation:**
 Hive will read the data from the source table and write it in the target file format.

------

### Method 2: Direct External Table on ORC/Parquet Data (Advanced)

If you already have data stored in ORC/Parquet format (exported from elsewhere), you can register it as an **EXTERNAL TABLE**.

```sql
CREATE EXTERNAL TABLE external_orc_table (
  id INT,
  name STRING,
  salary DOUBLE,
  department STRING
)
STORED AS ORC
LOCATION '/user/hive/external/orc_data/';
```

------

## 4.6 Verifying File Formats on HDFS

You can check the files using the Docker shell:

```bash
docker exec -it namenode bash
hdfs dfs -ls /user/hive/warehouse/employees_orc/
```

Files will typically look like `000000_0`, `000000_1`, etc.

To confirm format:

- ORC and Parquet files are binary—**not** readable with `cat`.
- You can use Hive's built-in `DESCRIBE FORMATTED`:

```sql
DESCRIBE FORMATTED employees_orc;
```

Look for the `InputFormat` and `Storage` rows—they should mention ORC or Parquet.

------

## 4.7 Comparing Performance

Try running a simple aggregation on both tables and compare response times (for large datasets):

```sql
SELECT department, AVG(salary) FROM employees_orc GROUP BY department;
SELECT department, AVG(salary) FROM employees_parquet GROUP BY department;
```

**Note:** For huge datasets, ORC will usually be faster in Hive.

------

## 4.8 Partitioned ORC and Parquet Tables

Combine the best of both worlds—partitioned + columnar!

```sql
CREATE TABLE sales_partitioned_orc (
  transaction_id STRING,
  customer_id STRING,
  amount DOUBLE,
  region STRING,
  day INT
)
PARTITIONED BY (year INT, month INT)
STORED AS ORC;
```

And for Parquet:

```sql
CREATE TABLE sales_partitioned_parquet (
  transaction_id STRING,
  customer_id STRING,
  amount DOUBLE,
  region STRING,
  day INT
)
PARTITIONED BY (year INT, month INT)
STORED AS PARQUET;
```

You can then use the **dynamic partitioning** process from Section 3 to populate these tables.

------

## 4.9 Best Practices and Tips

- Use **ORC** for Hive-centric, maximum performance on aggregations, storage savings, and indexing.
- Use **Parquet** for multi-tool compatibility (Spark, Presto, Pandas, etc.).
- Use **dynamic partitioning** with these formats for best scalability.
- Always convert raw data (CSV/TEXTFILE) to ORC or Parquet before running heavy analytics!

------

## 4.10 Quick Recap and Command-Line Checklist

**From inside your Hive Docker setup:**

1. **Create a table in ORC:**

   ```sql
   CREATE TABLE demo_orc (id INT, name STRING) STORED AS ORC;
   ```

2. **Create a table in Parquet:**

   ```sql
   CREATE TABLE demo_parquet (id INT, name STRING) STORED AS PARQUET;
   ```

3. **Load data (from another table):**

   ```sql
   INSERT INTO demo_orc SELECT * FROM demo_text;
   INSERT INTO demo_parquet SELECT * FROM demo_text;
   ```

4. **Check file formats:**

   ```sql
   DESCRIBE FORMATTED demo_orc;
   DESCRIBE FORMATTED demo_parquet;
   ```

5. **Inspect HDFS (Docker shell):**

   ```bash
   docker exec -it namenode bash
   hdfs dfs -ls /user/hive/warehouse/demo_orc/
   ```

------

## 4.11 Teaching Points for Learners

- *Columnar formats* (ORC, Parquet) are not optional—they’re critical for scalable big data analytics!
- Always use text/CSV as staging only. Convert to ORC/Parquet before production queries.
- Understand your workflow: choose ORC for Hive-first, Parquet for ecosystem interoperability.

------

**Next:**
 Section 5: **Hands-on Exercise—Analyzing a Large Dataset with Partitioned Hive Tables** (where we’ll use everything from Sections 1-4 in a real project).

------

---

# Section 5: Hands-on: Analyzing Web Sales Data (Student Version)

## Overview

In this hands-on activity, you will analyze an e-commerce dataset stored in Parquet format. This activity reinforces concepts you've learned about Hive, partitioning, and file formats by applying them to a real dataset.

You will:

1. Explore a Parquet file using Python
2. Create tables in Hive to store the data
3. Apply partitioning techniques
4. Write and run analytical queries

## 5.1 Preparing Your Environment

### Step 1: Check Your Docker Environment

First, make sure all your Docker services are running correctly:

```bash
docker compose ps
```

**Task**: Verify that all services (namenode, datanode, hive-server, etc.) show as "Up" or "Healthy".

### Step 2: Installing Python Tools for Exploration

We'll first explore the Parquet file using Python. Follow these steps to install Python and necessary libraries in your hive-server container:

```bash
# Connect to the container as root
docker exec -u root -it hive-server bash

# Update package information
apt-get update

# Install Python and pip
apt-get install -y python3 python3-pip

# Install pandas and pyarrow for Parquet support
pip3 install pandas pyarrow
```

### Step 3: Locate and Explore the Parquet File

The web_sales.parquet file should be available in your container. Let's explore it:

```bash
# Navigate to the examples directory
cd /opt/hive/examples/files

# Start Python
python3
```

In the Python shell, run:

```python
import pandas as pd

# Load the Parquet file
df = pd.read_parquet('web_sales.parquet')

# View the columns and data types
print(df.dtypes)

# See the first few rows
print(df.head())

# Get the number of rows
print(f"Number of rows: {len(df)}")

# Exit Python when done
exit()
```

**Task**: Based on the schema output, identify:

1. Which columns would be useful for analysis
2. Which columns might be good candidates for partitioning
3. Make note of the data types of key columns

**Hint**: Date-related columns (like those with "date" in the name) and columns with discrete values (like year, month, or region) are often good partitioning candidates.

## 5.2 Creating a Raw Table in Hive

Now, let's make this data available in Hive.

### Step 1: Copy the Parquet File to HDFS

```bash
# Still in the hive-server container
hdfs dfs -mkdir -p /user/hive/raw_data/
hdfs dfs -put /opt/hive/examples/files/web_sales.parquet /user/hive/raw_data/

# Verify the file was copied
hdfs dfs -ls /user/hive/raw_data/
```

### Step 2: Connect to Hive and Create a Raw Table

```bash
# Start Beeline
beeline -u jdbc:hive2://localhost:10000
```

**Task**: Create an external table pointing to the Parquet file.

**Hints**:

- Use `CREATE EXTERNAL TABLE` syntax
- The table should be called `web_sales_raw`
- Set `STORED AS PARQUET`
- Set the `LOCATION` to point to where you uploaded the file
- You don't need to define columns, as Parquet is self-describing

Your query should look something like:

```sql
CREATE EXTERNAL TABLE web_sales_raw
_______ AS PARQUET
_______ 'hdfs://namenode:8020/user/hive/raw_data/';
```

### Step 3: Explore the Table in Hive

**Task**: Run queries to explore the data in Hive.

**Hints**:

- Use `DESCRIBE web_sales_raw` to see the columns
- Use `SELECT * FROM web_sales_raw LIMIT 10` to see sample data
- Try `SELECT COUNT(*) FROM web_sales_raw` to see the total records

## 5.3 Preparing for Partitioning

Based on your exploration, you need to decide how to partition the data. From the schema, you can see that there are date-related columns like `ws_ship_date_sk`.

**Task**: Create a date dimension table with year and month information.

**Approach Options**:

1. Extract year and month from existing date columns
2. Create a simplified date dimension for this exercise

**Hint**: For this exercise, you can use a simplified approach like:

```sql
CREATE TABLE date_dim AS
SELECT 
  DISTINCT __________,  -- Choose a date column as key
  __________ as year,   -- Extract or generate year
  __________ as month   -- Extract or generate month 
FROM web_sales_raw
WHERE __________ IS NOT NULL;

-- Check your date dimension
SELECT * FROM date_dim LIMIT 10;
```

## 5.4 Creating a Partitioned Table

**Task**: Create a partitioned table that will store the web sales data organized by year and month.

**Hints**:

- Choose the most important columns from `web_sales_raw` (you don't need to include all of them)
- Important columns to consider:
  - `ws_item_sk` (product identifier)
  - `ws_quantity` (quantity sold)
  - `ws_sales_price` (price)
  - `ws_net_profit` (profit)
  - Date-related columns
- Partition by year and month
- Store as ORC for better performance

Your query will look something like:

```sql
CREATE TABLE web_sales_partitioned (
  -- Choose your columns here
  -- Remember to exclude partition columns from this list
)
PARTITIONED BY (__________, __________)
STORED AS ORC;
```

## 5.5 Loading Data with Dynamic Partitioning

### Step 1: Enable Dynamic Partitioning

**Task**: Enable dynamic partitioning in Hive.

**Hint**: You need to set at least these two properties:

```sql
SET hive.exec.dynamic.partition = _________;
SET hive.exec.dynamic.partition.mode = _________;
```

### Step 2: Insert Data with Dynamic Partitioning

**Task**: Write a query to insert data from `web_sales_raw` into your partitioned table.

**Hints**:

- Use `INSERT INTO TABLE ... PARTITION`
- Join with your date dimension table to get year and month
- Select all necessary columns from `web_sales_raw`
- Include the partition columns last in your SELECT

Your query might look like:

```sql
INSERT INTO TABLE web_sales_partitioned PARTITION (__________, __________)
SELECT 
  w.__________, -- columns from web_sales
  w.__________,
  -- other columns
  d.year,
  d.month
FROM web_sales_raw w
JOIN date_dim d ON w.__________ = d.__________;
```

### Step 3: Verify the Partitions

**Task**: Check what partitions were created.

**Hint**:

```sql
SHOW PARTITIONS web_sales_partitioned;
```

## 5.6 Writing Analytical Queries

Now comes the fun part! Write analytical queries to gain insights from your data.

**Task 1**: Calculate total sales by month and year.

**Hints**:

- Use aggregation functions like COUNT, SUM
- Group by year and month
- Consider columns like quantity, sales price, and profit

```sql
SELECT 
  year, 
  month, 
  COUNT(*) AS transaction_count,
  -- What other metrics would be useful?
FROM web_sales_partitioned
GROUP BY __________, __________
ORDER BY __________, __________;
```

**Task 2**: Find the top-selling products.

**Hints**:

- Group by product identifier
- Calculate metrics like times sold, total quantity, total revenue
- Order by a relevant metric
- Limit to a reasonable number of results

```sql
SELECT 
  __________, -- product identifier
  COUNT(*) AS times_sold,
  -- What other metrics would be useful?
FROM web_sales_partitioned
GROUP BY __________
ORDER BY __________ DESC
LIMIT 10;
```

**Task 3**: Test partition pruning performance.

**Hints**:

- Run a query that filters on partition columns
- Then run a similar query on the raw table
- Compare the execution times

```sql
-- Query on partitioned table
SELECT 
  COUNT(*) AS transaction_count,
  -- Other aggregations
FROM web_sales_partitioned
WHERE year = ______ AND month = ______;

-- Similar query on raw table with join
SELECT 
  COUNT(*) AS transaction_count,
  -- Other aggregations
FROM web_sales_raw w
JOIN date_dim d ON w.__________ = d.__________
WHERE d.year = ______ AND d.month = ______;
```

## 5.7 Challenge Tasks (Optional)

If you finish early, try these challenges:

1. **Create a report showing** sales trends over time (by month).
2. **Calculate profit margins** by product category.
3. **Identify** the best-performing time periods.
4. **Create a bucketed table** on top of your partitioned table for even better query performance when filtering by product ID.

## 5.8 Troubleshooting Guide

If you encounter issues:

1. **Problem**: Tables don't show up in Hive. **Solution**: Check your SQL syntax, especially quotes and semicolons.
2. **Problem**: Cannot see partitions. **Try**: `MSCK REPAIR TABLE web_sales_partitioned;`
3. **Problem**: Errors when reading Parquet file. **Check**: Ensure pyarrow is installed correctly, and the file path is correct.
4. **Problem**: ORC files not being created. **Alternative**: Try using `STORED AS PARQUET` instead.
5. **Problem**: Slow query performance. **Check**: Ensure you're using partition pruning in your WHERE clauses.

## Submission

Document your work with:

1. SQL scripts you created
2. Screenshots of query results
3. Answers to these reflection questions:
   - How did partitioning improve query performance?
   - What partitioning strategy would you recommend for this dataset in a production environment?
   - What additional optimizations could further improve performance?

---

## Appendixes

## Essential Columns for the Web Sales Analysis Activity

### Primary Identifiers

- `ws_item_sk` (int32) - Product/item identifier
- `ws_order_number` (int64) - Order identifier

### Transaction Details

- `ws_quantity` (float64) - Quantity of items sold
- `ws_sales_price` (object) - Sale price of items
- `ws_net_profit` (object) - Net profit from the sale
- `ws_net_paid` (object) - Amount paid (net)

### Temporal Data (for Partitioning)

- `ws_ship_date_sk` (float64) - Ship date identifier (useful for joining with date dimension)
- `ws_sold_time_sk` (float64) - Time of sale identifier

### Foreign Keys (for Additional Analysis)

- `ws_web_site_sk` (float64) - Website identifier
- `ws_web_page_sk` (float64) - Web page identifier
- `ws_bill_customer_sk` (float64) - Customer identifier (billing)
- `ws_ship_customer_sk` (float64) - Customer identifier (shipping)
- `ws_promo_sk` (float64) - Promotion identifier

### Additional Financial Columns

- `ws_wholesale_cost` (object) - Wholesale cost
- `ws_list_price` (object) - List price
- `ws_ext_sales_price` (object) - Extended sales price
- `ws_coupon_amt` (object) - Coupon amount

These columns cover the core aspects needed for the activity, including data for partitioning, sales analysis, and product performance metrics. Since many of the monetary columns are stored as objects (likely strings), students may need to cast them to appropriate numeric types for calculations.

---

# Converting String/Object Columns to Numeric Types in Hive

When working with columns stored as strings/objects (like `ws_sales_price` or `ws_net_profit` in your dataset), you'll need to cast them to numeric types before performing calculations. Here's how to do this in Hive:

## Basic Casting Methods in HiveQL

### 1. Using CAST Function

```sql
-- Basic syntax
CAST(column_name AS data_type)

-- Examples
CAST(ws_sales_price AS DECIMAL(10,2))
CAST(ws_net_profit AS DOUBLE)
```

### 2. Using :: Operator (Alternative Syntax)

```sql
-- Basic syntax
column_name::data_type

-- Examples
ws_sales_price::DECIMAL(10,2)
ws_net_profit::DOUBLE
```

## Practical Examples

### In SELECT Statements

```sql
-- Calculate total sales with proper numeric typing
SELECT 
  SUM(CAST(ws_sales_price AS DECIMAL(10,2))) AS total_sales,
  AVG(CAST(ws_net_profit AS DECIMAL(10,2))) AS avg_profit
FROM web_sales_raw;
```

### In Aggregation Queries

```sql
-- Analyze product performance with proper casting
SELECT 
  ws_item_sk,
  COUNT(*) AS times_sold,
  SUM(CAST(ws_quantity AS INT)) AS total_quantity,
  SUM(CAST(ws_sales_price AS DECIMAL(10,2))) AS total_revenue,
  SUM(CAST(ws_net_profit AS DECIMAL(10,2))) AS total_profit
FROM web_sales_raw
GROUP BY ws_item_sk
ORDER BY total_revenue DESC
LIMIT 10;
```

### In Table Creation

If you want to permanently store the data with proper types:

```sql
CREATE TABLE web_sales_proper_types AS
SELECT
  ws_item_sk,
  ws_order_number,
  ws_quantity,
  CAST(ws_sales_price AS DECIMAL(10,2)) AS ws_sales_price,
  CAST(ws_net_profit AS DECIMAL(10,2)) AS ws_net_profit,
  CAST(ws_net_paid AS DECIMAL(10,2)) AS ws_net_paid
FROM web_sales_raw;
```

## Tips for Students

1. **Check for NULL values**: Before casting, check if columns contain NULL values using `WHERE column_name IS NOT NULL`
2. **Handle invalid values**: Some values might not be convertible to numbers; use WHERE clauses to filter these out
3. **Choose appropriate precision**: For monetary values, DECIMAL(10,2) is usually good (10 digits total, 2 after decimal)
4. **Performance note**: Casting operations add processing overhead, so only cast columns you need for calculations
5. **Dealing with errors**: If you get conversion errors, examine the data for special characters or formatting issues

By properly casting these string/object columns to numeric types, you'll be able to perform accurate calculations and analysis on your web sales data.
