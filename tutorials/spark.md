

# Section 1: Spark Architecture and Core Concepts

## 1.1 What is Apache Spark?

Apache Spark is a powerful, open-source distributed computing system designed for fast, large-scale data processing. Unlike traditional MapReduce frameworks, Spark processes data in-memory, which dramatically improves performance for many types of operations. Originally developed at UC Berkeley's AMPLab in 2009, Spark has grown to become one of the most active open-source projects in the big data ecosystem.

At its core, Spark provides a unified computing engine that supports multiple data processing workloads, including batch processing, interactive queries, streaming, machine learning, and graph processing. This versatility eliminates the need for separate tools for different processing requirements, simplifying your data infrastructure and development process.

### Key Features That Make Spark Different

Spark stands out from other data processing frameworks for several important reasons:

**In-Memory Processing**: Spark can cache datasets in memory across operations, reducing disk I/O bottlenecks. This capability makes Spark up to 100 times faster than Hadoop MapReduce for certain workloads, especially for iterative algorithms and interactive data analysis.

**Unified Engine**: Rather than using separate systems for different needs, Spark provides a consistent programming model across batch, streaming, and interactive workloads. This unification makes development easier and more efficient.

**Rich APIs**: Spark offers high-level APIs in Java, Scala, Python, and R, making it accessible to a wide range of developers and data scientists. These APIs abstract away much of the complexity of distributed computing.

**Lazy Evaluation**: Spark doesn't execute operations immediately upon definition. Instead, it builds a directed acyclic graph (DAG) of the operations, optimizing the execution plan before running. This approach allows Spark to perform sophisticated optimizations automatically.

**Fault Tolerance**: Like Hadoop, Spark is designed to handle node failures gracefully in a distributed environment, ensuring reliable data processing even when individual machines fail.

### Comparing Spark with Traditional MapReduce

To better understand Spark's advantages, let's compare it with the traditional Hadoop MapReduce framework:

| Feature             | Hadoop MapReduce                                      | Apache Spark                                            |
| ------------------- | ----------------------------------------------------- | ------------------------------------------------------- |
| Processing Model    | Disk-based                                            | In-memory with disk fallback                            |
| Performance         | Good for very large datasets that don't fit in memory | Much faster (10-100x) for data that fits in memory      |
| Programming Model   | Map and Reduce steps                                  | Rich set of transformations and actions                 |
| Ease of Use         | Requires complex code for iterative algorithms        | Simple APIs for complex workflows                       |
| Supported Workloads | Primarily batch processing                            | Batch, interactive, streaming, ML, and graph processing |
| Language Support    | Java (primarily)                                      | Java, Scala, Python, R                                  |
| Recovery            | Automatic restart of failed tasks                     | Lineage-based recovery of lost partitions               |

### When to Use Spark

Spark is particularly well-suited for:

1. **Iterative algorithms**: Machine learning, graph processing, and other computations that revisit the same dataset multiple times
2. **Interactive analysis**: Data exploration and visualization where fast response times are important
3. **Stream processing**: Processing real-time data from sources like sensors, log files, or social media
4. **Complex ETL workflows**: Transforming data from multiple sources with complex business logic
5. **Unified applications**: Applications that combine batch, streaming, and interactive components

## 1.2 The Spark Ecosystem

The Spark ecosystem consists of a core processing engine surrounded by a set of specialized libraries that extend its capabilities for specific tasks:

### Spark Core

The foundation of the entire Spark platform, Spark Core provides:

- Distributed task dispatching
- Scheduling
- Basic I/O functionalities
- The API that defines Resilient Distributed Datasets (RDDs), Spark's fundamental data abstraction

### Spark SQL

Spark SQL enables working with structured data using either SQL or a DataFrame API. It allows you to:

- Query data using SQL statements
- Read data from various sources (Hive, JSON, Parquet, etc.)
- Join disparate data sources easily
- Leverage schema information for optimization

### Spark Streaming

Spark Streaming extends Spark's capabilities to process real-time data streams with the same programming model used for batch tasks. It:

- Divides streaming data into micro-batches
- Applies the same operations available for batch processing
- Integrates with many data sources like Kafka, Flume, and Kinesis

### MLlib (Machine Learning)

MLlib is Spark's scalable machine learning library that provides:

- Common algorithms for classification, regression, clustering, etc.
- Feature extraction and transformation utilities
- Model evaluation and hyperparameter tuning
- Tools for building ML pipelines

### GraphX

GraphX extends Spark for graph computation, offering:

- A graph processing library for analyzing connected data
- A collection of graph algorithms
- A unified API that treats graphs and collections interchangeably

### SparkR

SparkR is an R package that provides a lightweight frontend to use Spark from R, popular among data scientists for:

- Statistical computing
- Working with large datasets using familiar R syntax
- Integration with R's visualization capabilities

## 1.3 Spark Architecture and Components

Understanding Spark's architecture is essential for optimizing your applications and troubleshooting issues. Let's explore the key components:

### Cluster Manager

The cluster manager is responsible for acquiring resources across the Spark cluster. Spark can run on several cluster managers:

- **Standalone**: Spark's built-in cluster manager
- **YARN**: Hadoop's resource manager
- **Mesos**: A general-purpose cluster manager
- **Kubernetes**: Container orchestration platform

### Driver Program

The driver program is where your main application runs and creates a SparkContext (or SparkSession in newer versions), which coordinates the execution of your Spark job. The driver:

- Converts your application into tasks
- Schedules tasks on executors
- Coordinates their execution
- Collects the results

### Executors

Executors are worker processes responsible for executing the tasks assigned by the driver. Each executor:

- Runs on a worker node in the cluster
- Executes tasks in separate threads
- Stores computation results in memory, on disk, or both
- Communicates with the driver program

### SparkContext/SparkSession

The SparkContext (or SparkSession in Spark 2.0+) represents the connection to the Spark cluster and:

- Coordinates task execution
- Manages the application lifecycle
- Provides access to Spark's functionality
- Allows configuration of various parameters

### Distributed Execution Flow

Here's how a typical Spark application executes:

1. Your application creates a SparkSession or SparkContext
2. The context connects to the cluster manager
3. The cluster manager allocates resources (executors)
4. The driver converts your code into a sequence of tasks
5. Tasks are packaged and sent to executors
6. Executors run the tasks and report results/status back to the driver
7. Results from all tasks are combined to produce the final output

![Spark Architecture Diagram (Conceptual)]

### Data Distribution and Partitioning

Spark distributes data across the cluster in partitions, which are atomic units of parallelism:

- Each partition contains a subset of the data
- Partitions can be processed in parallel on different executors
- The number of partitions affects the degree of parallelism
- Default partitioning is based on input data, but can be controlled by the user

## 1.4 Spark Execution Model

Understanding how Spark executes your code helps you write more efficient applications. Let's explore the key concepts:

### RDDs, DataFrames, and Datasets

Spark has three main abstractions for distributed data:

**Resilient Distributed Datasets (RDDs)**: The fundamental data structure in Spark.

- Collections of objects spread across a cluster
- Immutable, meaning they cannot be changed once created
- Can be rebuilt if a node fails (resilient)
- Operations are tracked through lineage for fault tolerance

**DataFrames**: Distributed collections of data organized into named columns.

- Similar to tables in a relational database
- Provide schema information that enables optimizations
- More efficient than RDDs due to optimized execution plans
- Familiar for users coming from SQL or pandas

**Datasets**: Type-safe version of DataFrames available in Java and Scala.

- Combine the benefits of RDDs (type safety) and DataFrames (optimizations)
- Enable compile-time type checking
- Provide object-oriented programming interface

### Transformations vs. Actions

Spark operations fall into two categories:

**Transformations**: Create a new dataset from an existing one.

- Examples: map, filter, join, union
- Lazy evaluation - not executed immediately
- Build up a lineage of operations
- Can be narrow (no data shuffling needed) or wide (require shuffling)

**Actions**: Return a value to the driver program after running a computation.

- Examples: count, collect, save
- Trigger the execution of transformations
- Return results to the driver or write to storage

### Lazy Evaluation

One of Spark's key optimizations is lazy evaluation:

- Transformations are not executed until an action is called
- Spark builds a directed acyclic graph (DAG) of operations
- The DAG optimizer can reorder and combine operations for efficiency
- Unnecessary operations can be eliminated
- Data processing happens only when absolutely needed

### Jobs, Stages, and Tasks

When you execute an action in Spark, the work is divided hierarchically:

**Job**: A complete processing task triggered by an action.

- Contains one or more stages

**Stage**: A set of tasks that can be executed in parallel without shuffling data.

- Stages are separated by operations that require data redistribution (shuffle)
- Each stage consists of tasks that can run independently

**Task**: The smallest unit of work sent to an executor.

- Processes a single partition of data
- Runs on a single executor

### Spark UI and Monitoring

Spark provides a web interface to monitor your applications:

- Available by default on port 4040
- Shows jobs, stages, and tasks
- Displays memory usage and executor information
- Provides visualization of execution plans
- Helps identify bottlenecks and optimization opportunities

## 1.5 Hands-On: Getting Started with Spark

Let's start exploring Spark with some practical examples. In this section, we'll create our first Spark application to understand the basic concepts.

### Creating Your First SparkSession

The SparkSession is your entry point to Spark functionality. Open Jupyter at http://localhost:8889/ and create a new notebook. Then run:

```python
from pyspark.sql import SparkSession

# Create a SparkSession
spark = SparkSession.builder \
    .appName("FirstSparkApplication") \
    .getOrCreate()

# Print Spark version
print(f"Spark version: {spark.version}")

# Explore Spark configuration
print("\nSpark configuration:")
for item in spark.sparkContext.getConf().getAll():
    print(f"  {item[0]}: {item[1]}")
```

This code initializes a SparkSession, which is the unified entry point for all Spark functionality in Spark 2.0 and later. The `.builder` pattern is a fluent API for constructing the session with various configuration options.

### Creating and Manipulating Your First RDD

Now, let's create a simple RDD and perform some basic operations:

```python
# Create an RDD from a list of numbers
data = range(1, 101)  # Numbers from 1 to 100
rdd = spark.sparkContext.parallelize(data, 4)  # Distribute into 4 partitions

# Print information about the RDD
print(f"Number of partitions: {rdd.getNumPartitions()}")
print(f"RDD storage level: {rdd.getStorageLevel()}")

# Basic transformations
squared = rdd.map(lambda x: x * x)
filtered = squared.filter(lambda x: x % 10 == 0)  # Keep only multiples of 10

# Actions to execute the computation
result = filtered.collect()
print(f"Result count: {len(result)}")
print(f"First 10 results: {result[:10]}")
```

This example demonstrates several key Spark concepts:

- Creating an RDD using `parallelize` with a specified number of partitions
- Applying transformations (`map`, `filter`) to create new RDDs
- Using an action (`collect`) to trigger the computation and return results

### Understanding Lazy Evaluation in Action

Let's see how lazy evaluation works in practice:

```python
# Define a function we can use to track execution
def verbose_square(x):
    print(f"Computing square of {x}")
    return x * x

# Create an RDD and transform it
data = range(1, 11)  # Small range for demonstration
rdd = spark.sparkContext.parallelize(data, 2)
squared = rdd.map(verbose_square)

print("RDD defined, but nothing computed yet due to lazy evaluation")
print("No output from verbose_square function until an action is called")

# Now trigger computation with an action
print("\nCalling collect() to trigger computation:")
result = squared.collect()
print(f"\nFinal result: {result}")
```

Notice how the `verbose_square` function doesn't execute until you call the `collect` action. This demonstrates Spark's lazy evaluation at work.

### Working with DataFrames

Let's move from RDDs to DataFrames, which offer a more structured and optimized way to work with data:

```python
# Initialize SparkSession with Hive support disabled
from pyspark.sql import SparkSession

# Create a SparkSession
spark = SparkSession.builder \
    .appName("SparkBasics") \
    .config("spark.sql.catalogImplementation", "in-memory") \
    .getOrCreate()

# Create a simple DataFrame from a list of tuples
data = [
    (1, "John", 28, "New York"),
    (2, "Mary", 34, "San Francisco"),
    (3, "Bob", 42, "Chicago"),
    (4, "Alice", 25, "Los Angeles"),
    (5, "David", 31, "Houston")
]
columns = ["id", "name", "age", "city"]
df = spark.createDataFrame(data, schema=columns)

# Show the DataFrame
print("DataFrame preview:")
df.show()

# Get schema information
print("\nDataFrame schema:")
df.printSchema()

# Basic DataFrame operations
print("\nPeople older than 30:")
df.filter(df.age > 30).show()

# SQL-style operations
df.createOrReplaceTempView("people")
spark.sql("SELECT * FROM people WHERE age > 30 ORDER BY age").show()

# Save the DataFrame to HDFS in Parquet format
hdfs_parquet_path = "hdfs://namenode:8020/user/root/data/people.parquet"
df.write.mode("overwrite").parquet(hdfs_parquet_path)
print(f"Data saved to {hdfs_parquet_path}")

# Read it back from HDFS
loaded_df = spark.read.parquet(hdfs_parquet_path)
print("\nData loaded from parquet file in HDFS:")
loaded_df.show()
```

This example shows how to:

- Create a DataFrame from in-memory data
- Display the data and schema
- Perform filtering operations using both the DataFrame API and SQL
- Register a temporary view for SQL queries

### Exploring the Execution Plan

Spark's Catalyst optimizer generates execution plans for your queries. Let's see how to examine these plans:

```python
# Create a more complex DataFrame
from pyspark.sql.functions import col, avg, sum as sum_col

# Filter and aggregate
query = df.filter(col("age") > 25) \
         .groupBy("city") \
         .agg(avg("age").alias("avg_age"), sum_col("age").alias("total_age"))

# Examine the execution plan
print("Logical and physical plans:")
query.explain(True)

# Execute the query
print("\nQuery results:")
query.show()
```

The `explain` method shows:

1. The parsed logical plan
2. The analyzed logical plan
3. The optimized logical plan
4. The physical plan that will be executed

Understanding these plans is crucial for optimizing your Spark applications.

### Saving and Loading Data

Finally, let's look at how to save and load data in Spark:

```python
# Save the DataFrame to a parquet file in HDFS
hdfs_parquet_path = "hdfs://namenode:8020/user/root/data/people.parquet"
df.write.mode("overwrite").parquet(hdfs_parquet_path)
print(f"Data saved to {hdfs_parquet_path}")

# Read it back from HDFS
loaded_df = spark.read.parquet(hdfs_parquet_path)
print("\nData loaded from parquet file in HDFS:")
loaded_df.show()

# Verify the file was created in HDFS
print("\nVerifying parquet files in HDFS:")
!hdfs dfs -ls /user/root/data/people.parquet

# Try other formats (CSV) in HDFS
hdfs_csv_path = "hdfs://namenode:8020/user/root/data/people.csv"
df.write.mode("overwrite").option("header", "true").csv(hdfs_csv_path)
print(f"\nData saved to {hdfs_csv_path}")

# Read CSV with schema inference from HDFS
csv_df = spark.read.option("header", "true").option("inferSchema", "true").csv(hdfs_csv_path)
print("\nData loaded from CSV file in HDFS:")
csv_df.show()

# Verify the CSV file was created in HDFS
print("\nVerifying CSV files in HDFS:")
!hdfs dfs -ls /user/root/data/people.csv

# Optional: For comparison, also save a local copy using pandas (for small data only)
print("\nFor small datasets, alternatively using pandas for local storage:")
local_csv_path = "/tmp/people_local.csv"
df.toPandas().to_csv(local_csv_path, index=False)
print(f"Data saved locally to {local_csv_path} using pandas")
!cat {local_csv_path}
```

This demonstrates how to:

- Save data in Parquet format (Spark's preferred format for performance)
- Load Parquet files back into DataFrames
- Work with CSV files, including header and schema options

## 1.6 Practice Exercises

### Exercise 1: Basic RDD Operations

Create an RDD containing the numbers 1 through 100. Perform the following operations:

1. Filter out all odd numbers
2. Multiply each remaining number by 3
3. Find the sum of all numbers greater than 50

```python
# Your solution here
# Example structure:
numbers = range(1, 101)
rdd = spark.sparkContext.parallelize(numbers)

# Filter out odd numbers
# ...

# Multiply by 3
# ...

# Sum numbers > 50
# ...
```

### Exercise 2: Working with DataFrames

Create a DataFrame representing a list of products with columns:

- product_id
- name
- category
- price

Perform the following operations:

1. Find the average price by category
2. Find the most expensive product in each category
3. Find all products with price above the average price of their category

```python
# Your solution here
```

### Exercise 3: Spark SQL

Register the products DataFrame from Exercise 2 as a temporary view called "products". Then:

1. Write a SQL query to find the total value of all products by category
2. Find products that have a price at least 20% higher than the average price in their category
3. Create a new column that classifies products as "Budget", "Regular", or "Premium" based on their price

```python
# Your solution here
```

## 1.7 Summary

In this section, we've explored the fundamental concepts of Apache Spark:

- Spark's architecture and distributed computing model
- Core components including RDDs, DataFrames, and SparkSession
- The difference between transformations and actions
- Spark's lazy evaluation strategy
- Basic operations with RDDs and DataFrames
- How to examine execution plans

These concepts provide the foundation for understanding how Spark processes data at scale. In the next section, we'll dive deeper into RDDs, the core abstraction behind Spark's distributed computing power.

Key takeaways:

- Spark processes data in-memory for faster performance than disk-based systems
- Spark operations are either transformations (lazy) or actions (eager)
- Spark builds a DAG of operations before execution for optimization
- The SparkSession is your entry point to Spark functionality
- DataFrames provide a structured API with optimization opportunities
- Understanding execution plans helps you optimize your Spark applications

In the next section, we'll explore RDDs in greater depth, including advanced operations, persistence strategies, and best practices for distributed data processing.

---

---



# Section 2: Hands-on ETL Pipeline with Complex Joins

In this section, we'll build a complete ETL (Extract, Transform, Load) pipeline using Apache Spark. This hands-on exercise will help you apply Spark concepts to solve a realistic data processing challenge.

## 2.1 Introduction: The Business Case

You're working with an e-commerce company that needs to analyze their sales data. They have four main datasets:

- **Customers**: Information about registered users
- **Products**: Details about items for sale
- **Orders**: Transaction records of purchases
- **Order Items**: Line items within each order

Your task is to build an ETL pipeline that combines these datasets to answer business questions like:

- Which customer segments generate the most revenue?
- What are the best-selling product categories?
- How do sales trends vary over time?

## 2.2 Setting Up Your Spark Environment

First, let's initialize a SparkSession with optimized settings for stability:

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, when, to_date, date_format, datediff
from pyspark.sql.functions import current_date, count, sum as sum_col
import os

# Initialize SparkSession with optimized settings
spark = SparkSession.builder \
    .appName("ETL Pipeline Exercise") \
    .config("spark.sql.catalogImplementation", "in-memory") \
    .config("spark.default.parallelism", "4") \
    .config("spark.sql.shuffle.partitions", "4") \
    .getOrCreate()

print(f"Spark Version: {spark.version}")
```

By configuring our SparkSession with:

- `spark.sql.catalogImplementation` as "in-memory" (to avoid Hive metastore issues)
- Controlled parallelism settings (to ensure stable performance)

This code sets up our connection to Spark - like opening a communication channel to talk to the big data processing engine.

We're creating a SparkSession (the main entry point for Spark functionality) with some specific settings:

- We name our application "ETL Pipeline Exercise" so we can identify it in monitoring tools
- We use "in-memory" catalog implementation instead of Hive, which makes our code work without needing extra database services
- We limit processing to 4 parallel tasks and 4 shuffle partitions to make our demonstrations run smoothly

These settings create a stable, predictable environment for our ETL work - kind of like setting up a proper workbench before starting a home improvement project.

We prepare a solid foundation for our ETL work.

## 2.3 Extract: Loading and Exploring the Datasets

Now we'll load our datasets from HDFS and explore their structure:

```python
# Define HDFS paths for our datasets
HDFS_BASE = "hdfs://namenode:8020/user/root/data/etl"
CUSTOMERS_PATH = f"{HDFS_BASE}/customers.csv"
PRODUCTS_PATH = f"{HDFS_BASE}/products.csv"
ORDERS_PATH = f"{HDFS_BASE}/orders.csv"
ORDER_ITEMS_PATH = f"{HDFS_BASE}/order_items.csv"

# Load CSV data with schema inference
print("Loading datasets from HDFS...")

customers = spark.read.option("header", "true").option("inferSchema", "true").csv(CUSTOMERS_PATH)
products = spark.read.option("header", "true").option("inferSchema", "true").csv(PRODUCTS_PATH)
orders = spark.read.option("header", "true").option("inferSchema", "true").csv(ORDERS_PATH)
order_items = spark.read.option("header", "true").option("inferSchema", "true").csv(ORDER_ITEMS_PATH)

# Verify data loaded correctly
print(f"Customers: {customers.count()} records")
print(f"Products: {products.count()} records")
print(f"Orders: {orders.count()} records")
print(f"Order Items: {order_items.count()} records")
```

Here we're loading four CSV files from our HDFS storage system (a distributed file system designed for big data).

For each dataset, we tell Spark two important things:

- "header" = "true" means the first row contains column names, not data
- "inferSchema" = "true" tells Spark to examine the data and guess the appropriate data types (numbers, text, dates)

After loading each dataset, we count how many records it contains. This simple check confirms our data loaded successfully before we invest time in processing it.

Think of this step as opening all the files we need for our analysis project and making sure they contain the expected information.

Let's examine the structure of each dataset:

```python
# Explore the data structure
print("\nCustomers schema:")
customers.printSchema()
print("\nCustomers sample:")
customers.show(5)

print("\nProducts schema:")
products.printSchema()
print("\nProducts sample:")
products.show(5)

print("\nOrders schema:")
orders.printSchema()
print("\nOrders sample:")
orders.show(5)

print("\nOrder Items schema:")
order_items.printSchema()
print("\nOrder Items sample:")
order_items.show(5)
```

By examining the schemas and sample data, we get a clear understanding of our datasets before transforming them.

Before transforming our data, we're exploring what we have by examining:

1. The schema (column names and data types) of each dataset
2. A sample of the first 5 rows from each dataset

This exploration is like reviewing ingredients before cooking - we need to understand what we have before we can work with it effectively.

The schema shows us the structure (what columns exist and what type of data they contain), while the sample rows help us see what actual values look like. Together, they give us a clear picture of our data before we start transforming it.

## 2.4 Transform: Data Cleaning and Enrichment

Now we'll clean and enhance our data by adding useful derived columns:

```python
# Clean and enrich customer data
customers_clean = customers.withColumn(
    "customer_tenure_days",  # Add days since registration
    datediff(current_date(), to_date(col("registration_date")))
).withColumn(
    "segment_value",  # Add numeric value for each segment
    when(col("segment") == "Premium", 3)
    .when(col("segment") == "Standard", 2)
    .when(col("segment") == "Basic", 1)
    .otherwise(0)
)

print("\nTransformed customers data:")
customers_clean.show(5)

# Clean and enrich order data
orders_clean = orders.withColumn(
    "order_month",  # Extract month for time-based analysis
    date_format(to_date(col("order_date")), "yyyy-MM")
).withColumn(
    "is_completed",  # Binary flag for completed orders
    when(col("status") == "Completed", 1)
    .when(col("status") == "Shipped", 1)
    .otherwise(0)
)

print("\nTransformed orders data:")
orders_clean.show(5)
```

We've enriched our data by:

- Adding `customer_tenure_days` to see how long customers have been registered
- Creating `segment_value` to give numeric values to customer segments
- Extracting `order_month` for time-based analysis
- Adding `is_completed` flag to identify completed or shipped orders

In this step, we're enhancing our **customer data** by adding two useful new columns:

1. "customer_tenure_days" calculates how many days each customer has been registered by finding the difference between today and their registration date. This helps us understand customer loyalty.

2. "segment_value" converts text segments like "Premium" into numeric values (3 for Premium, 2 for Standard, 1 for Basic). These numbers can be useful for calculations and machine learning models.

These transformations are like adding calculated columns in a spreadsheet - they derive new, useful information from our existing data without changing the original values.

Now we're enhancing our **order data** with two new columns:

1. "order_month" extracts just the year and month (YYYY-MM format) from the full order date. This simplifies time-based analysis by grouping orders by month.

2. "is_completed" creates a simple 1/0 flag indicating whether an order is complete (status is either "Completed" or "Shipped"). This makes it easy to filter for completed orders.

These transformations prepare our data for analysis by extracting exactly the information we need in the most convenient format. The new columns make time-trend analysis and filtering much simpler in later steps.

## 2.5 Transform: Quality Checks and Caching

Before proceeding to complex joins, let's check data quality:

```python
# Perform data quality checks
print("\nOrder status distribution:")
orders.groupBy("status").count().show()

print("\nCustomer segment distribution:")
customers.groupBy("segment").count().show()

# Cache frequently used DataFrames for better performance
print("\nCaching cleaned datasets for better performance...")
customers_clean.cache()
orders_clean.cache()
customers_clean.count()  # Force caching
orders_clean.count()     # Force caching
```

The quality checks help us understand our data distribution, while caching improves performance for subsequent operations.

Before proceeding with more complex analysis, we're performing basic quality checks by counting:

1. How many orders exist for each status value
2. How many customers belong to each segment

These counts give us a quick overview of our data distribution and help catch obvious issues. For example, if we see unexpected status values or if certain segments have surprisingly few customers, we might need to investigate further.

We're also caching our cleaned datasets in memory. Caching means Spark keeps this data readily available instead of recalculating it every time it's needed. The count() operations force Spark to actually process the data now (instead of delaying due to lazy evaluation) and store the results in memory.



## 2.6 Transform: Joining Data for Business Insights

Now we'll combine our datasets with join operations:

```python
# Join orders with customers for customer analysis
print("\nJoining orders with customers...")
customer_orders = orders_clean.join(
    customers_clean,
    orders_clean.customer_id == customers_clean.customer_id
).select(
    orders_clean.order_id,
    orders_clean.customer_id,
    customers_clean.segment,
    customers_clean.city,
    orders_clean.order_date,
    orders_clean.total_amount,
    orders_clean.status
)

print("\nCustomer orders (sample):")
customer_orders.show(5)

# Join order items with products for product analysis
print("\nJoining order items with products...")
order_details = order_items.join(
    products,
    order_items.product_id == products.product_id
).select(
    order_items.order_id,
    order_items.product_id,
    products.name.alias("product_name"),
    products.category,
    order_items.quantity,
    order_items.price,
    (order_items.quantity * order_items.price).alias("line_total")
)

print("\nOrder details (sample):")
order_details.show(5)
```

These joins connect our datasets to provide integrated views:

- `customer_orders` links customer information with their order history
- `order_details` connects products with order items and calculates line totals

Now we're combining our separate datasets to create more useful, integrated views:

1. First, we join orders with customers by matching on customer_id. This connects each order to information about the customer who placed it. We then select just the columns we need from this joined data.

2. Similarly, we join order items with products by matching on product_id. This links each ordered item to details about the product. We also calculate "line_total" (quantity × price) to see the total for each line item.

These joins are like connecting related tables in a database. For example, before the join, we knew an order was placed by customer #123, but after joining with customer data, we can see it was placed by John Smith from New York in the Premium segment.



## 2.7 Transform: Creating Business Analytics

Let's create analytics that address our business questions:

```python
# 1. Revenue by customer segment
segment_revenue = customer_orders.groupBy("segment") \
    .agg(sum_col("total_amount").alias("total_revenue"), 
         count("order_id").alias("order_count")) \
    .orderBy(col("total_revenue").desc())

print("\nRevenue by customer segment:")
segment_revenue.show()

# 2. Top selling product categories
category_sales = order_details.groupBy("category") \
    .agg(sum_col("line_total").alias("total_revenue"), 
         sum_col("quantity").alias("units_sold")) \
    .orderBy(col("total_revenue").desc())

print("\nSales by product category:")
category_sales.show()

# 3. Monthly sales trends
monthly_sales = orders_clean \
    .filter(col("status").isin("Completed", "Shipped")) \
    .groupBy("order_month") \
    .agg(sum_col("total_amount").alias("monthly_revenue")) \
    .orderBy("order_month")

print("\nMonthly sales trends:")
monthly_sales.show()
```

These analytics answer key business questions:

- Which customer segments generate the most revenue?
- What are the best-selling product categories?
- How do sales trends vary over time?

Here we're creating three key business analytics reports:

1. "segment_revenue" shows total sales and order count for each customer segment. This answers "Which customer segments generate the most revenue?"

2. "category_sales" calculates total revenue and units sold for each product category. This tells us "What are the best-selling product categories?"

3. "monthly_sales" tracks revenue over time by grouping completed/shipped orders by month. This reveals "How do sales trends vary over time?"

In each case, we're using groupBy() to organize our data into categories, then agg() to calculate totals within each group. This is similar to creating pivot tables in Excel, where you summarize data by categories to spot patterns and trends.



## 2.8 Transform: Advanced Analytics with Window Functions

Now let's perform more sophisticated analytics using window functions:

```python
from pyspark.sql.window import Window
from pyspark.sql.functions import dense_rank, row_number, percent_rank

# Find top products in each category
windowSpec = Window.partitionBy("category").orderBy(col("total_revenue").desc())

top_products = order_details \
    .groupBy("category", "product_id", "product_name") \
    .agg(sum_col("line_total").alias("total_revenue"), 
         sum_col("quantity").alias("units_sold")) \
    .withColumn("rank_in_category", dense_rank().over(windowSpec)) \
    .filter(col("rank_in_category") <= 3)  # Top 3 products per category

print("\nTop 3 products in each category:")
top_products.show()

# Find high-value customers
customer_value_window = Window.orderBy(col("total_spent").desc())

valuable_customers = customer_orders \
    .groupBy("customer_id", "segment") \
    .agg(sum_col("total_amount").alias("total_spent"), 
         count("order_id").alias("order_count")) \
    .withColumn("customer_rank", row_number().over(customer_value_window)) \
    .withColumn("percentile", percent_rank().over(customer_value_window)) \
    .filter(col("customer_rank") <= 10)  # Top 10 customers

print("\nTop 10 most valuable customers:")
valuable_customers.show()
```

Window functions allow us to:

- Rank products within each category to find top performers
- Identify the most valuable customers across the entire dataset

This section uses advanced window functions to perform more sophisticated analysis:

1. For "top_products," we:
   - Group sales by product within each category
   - Use a window function to rank products by revenue within their category
   - Keep only the top 3 products in each category

2. For "valuable_customers," we:
   - Calculate each customer's total spending and order count
   - Use window functions to rank customers by spending and calculate their percentile
   - Filter to show only the top 10 highest-spending customers

Window functions are powerful because they let us perform calculations across groups of rows. For example, the "dense_rank" function ranks products within each category, allowing us to identify the best sellers in Electronics separately from the best sellers in Clothing.



## 2.9 Load: Saving Results (Optional)

Finally, we can save our analytics results for downstream applications:

```python
# Save results to HDFS (uncomment if needed)
print("\nSaving analytics results (optional)...")

# segment_revenue.write.mode("overwrite").parquet(f"{HDFS_BASE}/segment_revenue.parquet")
# category_sales.write.mode("overwrite").parquet(f"{HDFS_BASE}/category_sales.parquet")
# monthly_sales.write.mode("overwrite").parquet(f"{HDFS_BASE}/monthly_sales.parquet")
# top_products.write.mode("overwrite").parquet(f"{HDFS_BASE}/top_products.parquet")

print("\nETL pipeline complete!")
```

The final step in our ETL pipeline would typically be saving our processed data for future use. Although these commands are commented out (not actually running), they show how we would store our results:

1. The "write" method saves a DataFrame to storage
2. "mode("overwrite")" replaces any existing data at that location
3. "parquet" specifies a columnar file format that's efficient for analytics

Parquet is an excellent choice for analytics because it:

- Stores data in a compressed, columnar format
- Provides fast query performance
- Preserves schema information
- Works well with many big data tools

Saving our results completes the ETL pipeline, making our processed data available for dashboards, reports, or other applications.



## 2.10 Practice Exercises

Now that you've seen a complete ETL pipeline in action, try these exercises to deepen your understanding:

1. **Customer Segmentation Analysis**: Create a more sophisticated customer segmentation based on total spending, purchase frequency, and recency of last purchase.
2. **Product Affinity Analysis**: Identify which products are frequently purchased together by finding all pairs of products that appear in the same order and calculating how often each pair occurs.
3. **Performance Optimization**: Improve the ETL pipeline's performance by experimenting with:
   - Different partition counts
   - Strategic caching
   - Broadcast joins for small tables

For each exercise, compare your approach and results with the examples shown in this tutorial.

## 2.11 Summary

In this hands-on section, you've built a complete ETL pipeline with Apache Spark that:

- Loaded and explored multiple datasets
- Cleaned and enriched the data
- Joined datasets to create integrated views
- Created business analytics through aggregations
- Applied advanced analytics using window functions

These skills form the foundation of real-world data processing with Spark and can be applied to many different business scenarios and data domains.

