# Apache Flink Tutorial for Beginners

## Introduction to Apache Flink

### What is Apache Flink?

Apache Flink is an open-source stream processing framework designed for high-throughput, low-latency data processing over continuous data streams. Think of Flink as a powerful engine that processes data as it arrives rather than waiting to collect it all first.

Unlike traditional batch processing systems that work with fixed, stored datasets, Flink treats everything as a stream of events. This allows applications to analyze and react to data in real-time, which is crucial for use cases where immediate insights matter. For example, imagine monitoring credit card transactions to detect fraud as it happens, rather than discovering it hours later in a batch report.

The name "Flink" means "quick" or "nimble" in German, which reflects its design philosophy of processing data rapidly as it flows through the system.

### Key Features and Advantages

**Unified Stream and Batch Processing**

Flink offers a unique approach to data processing by treating batch processing as a special case of stream processing. In Flink's world, a batch is simply a stream with a beginning and an end (a "bounded" stream). This unification simplifies application development, as the same code can process both real-time data and historical data.

Imagine building a recommendation engine that works the same way whether it's processing years of historical user behavior or real-time clicks happening right now. This is what Flink enables.

**Exactly-Once Processing Guarantees**

One of Flink's most important features is its ability to guarantee that each event is processed exactly once, even when failures occur. This is critical for applications where accuracy is paramount, such as financial transactions or billing systems, where processing an event twice or missing it altogether could be disastrous.

This is like having a postal system that guarantees every letter arrives exactly once—no duplicates, no lost mail—even if the mail truck breaks down mid-route.

**Advanced State Management**

Flink excels at maintaining application state—the information you need to remember between events. For example, if you're calculating a running average of temperatures from sensors, you need to keep track of the sum and count of previous readings.

Flink manages this state in a distributed, fault-tolerant manner, allowing applications to maintain large amounts of data reliably across a cluster of machines. The state is automatically included in checkpoints and can be restored if failures occur.

**Sophisticated Time Handling**

In the real world, data doesn't always arrive in order. Flink has sophisticated support for different notions of time:

- Event time (when events actually occurred)
- Processing time (when Flink processes them)
- Ingestion time (when events enter the Flink system)

This is crucial for applications that need to process out-of-order data correctly. For example, if you're analyzing network traffic patterns and some data arrives late due to network delays, Flink can still process it as if it arrived on time.

**High Performance and Scalability**

Flink is designed for high-throughput, low-latency processing. It can scale horizontally by adding more machines to handle increasing data volumes, and it efficiently uses resources to maximize performance.

Large organizations like Alibaba use Flink to process billions of events per day with sub-second latency, demonstrating its ability to operate at massive scale.

### Flink vs. Other Stream Processing Frameworks

To better understand Flink's position in the stream processing landscape, let's compare it with other popular frameworks:

**Apache Flink vs. Apache Spark Streaming**

Spark Streaming uses micro-batching, where it collects data into small batches and processes them together. This introduces some latency (typically seconds). In contrast, Flink is a true streaming system that processes each event individually as it arrives, achieving much lower latency (milliseconds).

Think of the difference as similar to that between a train (Spark) that leaves the station when enough passengers have boarded versus a continuous conveyor belt (Flink) that moves items without waiting.

Spark has historically had better batch processing capabilities and a larger ecosystem, while Flink offers superior stream processing with better state management and event time handling.

**Apache Flink vs. Apache Kafka Streams**

Kafka Streams is tightly integrated with Apache Kafka and runs on the same JVM as the application using it. It's simpler but less feature-rich than Flink. Flink, on the other hand, is a standalone system with more sophisticated processing capabilities and can connect to many different sources beyond Kafka.

Kafka Streams is like having a small food processor in your kitchen for simple tasks, while Flink is more like a full commercial kitchen with specialized equipment for complex dishes.

**Apache Flink vs. Apache Storm**

Storm was one of the earliest distributed stream processing systems. Like Flink, it processes data in real-time, but it provides weaker guarantees (at-least-once rather than exactly-once by default) and has less sophisticated state management and time handling.

Flink has largely superseded Storm in many use cases due to its more advanced features and better performance characteristics.

### Flink Architecture

Flink's architecture consists of several layers that work together to enable high-performance stream processing:

![img](https://nightlies.apache.org/flink/flink-docs-master/fig/stack.png)

**Runtime Layer**

At the core of Flink is its distributed streaming dataflow engine. This layer is responsible for:

- Executing dataflow programs in a parallel and distributed manner
- Managing memory and CPU resources
- Coordinating the execution of tasks across a cluster
- Providing fault tolerance through checkpointing

Think of this as Flink's engine room, where the actual work happens.

**APIs and Libraries Layer**

Flink provides multiple APIs at different levels of abstraction:

1. **ProcessFunction API**: The lowest-level API, offering fine-grained control over state and time.
2. **DataStream/DataSet API**: The core APIs for stream and batch processing, providing operations like map, filter, aggregate, and window.
3. **Table API**: A relational API that allows you to write SQL-like queries using a fluent API.
4. **SQL**: Standard SQL interface for defining stream processing pipelines.
5. **Libraries**: Higher-level libraries for specific use cases like Complex Event Processing (CEP), Machine Learning, and Graph Processing.

As you move up this stack, you trade some flexibility for ease of use and expressiveness.

**Deployment Layer**

Flink applications can be deployed in various ways:

- **Standalone**: Running Flink directly on a cluster of machines
- **YARN**: Deploying on Hadoop's resource manager
- **Kubernetes**: Running in containers on Kubernetes
- **Docker**: Using Docker containers for deployment

**Runtime Components**

When a Flink application runs, it consists of the following components:

- **JobManager**: The coordinator of a Flink deployment. It schedules tasks, coordinates checkpoints, and handles recovery in case of failures. Think of it as the supervisor that oversees the entire operation.
- **TaskManagers**: The worker processes that execute the actual data processing tasks. Each TaskManager provides processing slots where tasks run, and they communicate with each other to exchange data. These are like the workers on a factory floor carrying out specific operations.
- **Client**: Prepares and submits dataflow programs to the JobManager. This is like the architect who designs the plan and hands it to the supervisor.

When a Flink program is submitted:

1. The client transforms it into a dataflow graph
2. The JobManager schedules tasks on available TaskManagers
3. The TaskManagers execute these tasks and exchange data
4. Results flow to the specified sinks (outputs)

This architecture allows Flink to scale from simple applications running on a laptop to massive distributed applications processing petabytes of data across hundreds of machines.

## Core Concepts

### Streams and Transformations

**Streams: The Foundation of Flink**

In Flink, everything is modeled as a stream—a continuous flow of data records or events. Streams can be:

- **Unbounded**: With no defined end, continuously processing as data arrives (like a Twitter feed)
- **Bounded**: With a defined beginning and end (like a CSV file)

A stream might consist of credit card transactions, temperature readings from IoT sensors, user clicks on a website, or any other type of event data.

**Transformations: Operations on Streams**

Transformations are operations that take one or more streams as input and produce one or more streams as output. They're the building blocks you use to create data processing pipelines.

Let's look at the most common transformations:

**Element-wise operations** work on individual records:

- **Map**: Applies a function to each element, transforming it to a potentially different type. For example, converting temperatures from Celsius to Fahrenheit: `stream.map(temp -> (temp * 9/5) + 32)`.

- **Filter**: Retains only elements that satisfy a condition, like keeping only temperatures above 100°F: `stream.filter(temp -> temp > 100)`.

- **FlatMap**: Similar to map, but each input element can produce zero, one, or more output elements. Useful for tasks like splitting sentences into words:

  ```
  "Hello world" -> ["Hello", "world"]
  ```

**Aggregation operations** combine multiple elements:

- **KeyBy**: Groups the stream by a key, like grouping temperature readings by sensor ID: `stream.keyBy(reading -> reading.getSensorId())`.

- **Reduce**: Combines elements in a group using a reducing function, such as calculating the maximum temperature per sensor:

  ```
  stream.keyBy(reading -> reading.getSensorId())
        .reduce((r1, r2) -> r1.getTemp() > r2.getTemp() ? r1 : r2)
  ```

- **Aggregate**: A more general form of reduction where the output type can differ from the input type.

**Multi-stream operations** work with multiple streams:

- **Union**: Combines multiple streams of the same type into a single stream, like merging temperature readings from different sources.
- **Connect**: Combines streams of different types, allowing them to share state.
- **Join**: Combines elements from two streams based on a key and a time window, such as joining purchase events with shipping events by order ID.

**Example Pipeline**

Here's what a simple Flink pipeline might look like in Python:

```python
# Create a stream from a source
data_stream = env.from_source(kafka_source, watermark_strategy, "Kafka Source")

# Apply transformations
filtered_stream = data_stream.filter(lambda event: event['temperature'] > 25.0)
alerts = filtered_stream.map(lambda event: {
    'sensor_id': event['sensor_id'],
    'alert': f"High temperature: {event['temperature']}°C",
    'timestamp': event['timestamp']
})

# Send the results to a sink
alerts.sink_to(kafka_sink)
```

This pipeline reads events from Kafka, filters for high temperatures, transforms them into alerts, and sends the alerts back to another Kafka topic.

### Time Semantics (Event Time, Processing Time, Ingestion Time)

Time is a fundamental concept in stream processing. Flink supports three different notions of time, each with its own use cases and trade-offs:

**Event Time: When Events Actually Happened**

Event time refers to the time when an event actually occurred in the real world, typically embedded within the data itself as a timestamp. For example, when a temperature sensor recorded a reading or when a user clicked on a website.

Event time is the most meaningful notion of time for most applications because it reflects the actual timeline of events, independent of when they're processed. This allows Flink to handle:

- Out-of-order events (events arriving late)
- Backfilling historical data
- Consistent results regardless of processing delays

Using event time is like sorting historical letters by their written date, not by when they arrived in your mailbox.

The challenge with event time is that Flink needs to know how long to wait for late events before considering a time window complete. This is addressed using watermarks, which we'll discuss shortly.

**Processing Time: When Events Are Processed**

Processing time is simply the wall-clock time of the machine processing the event. It's measured as the current time when an operation is being performed.

Advantages of processing time:

- Simplest to implement
- Lowest latency (no waiting for late events)
- No need for timestamps in the data

Disadvantages:

- Results can vary if you rerun the same data
- Affected by backpressure, network delays, and varying processing speeds
- Doesn't handle out-of-order events well

Processing time is like processing tasks in your inbox strictly by when you happen to see them, regardless of when they were sent.

**Ingestion Time: When Events Enter Flink**

Ingestion time is a hybrid approach where Flink assigns timestamps to events when they first enter the system. It's like processing time, but consistent across the entire pipeline.

Advantages:

- More consistent than processing time
- No need for timestamps in the data
- Somewhat handles backpressure issues

Disadvantages:

- Still doesn't handle out-of-order events well
- Results still vary upon reprocessing

**Choosing the Right Time Semantic**

The choice depends on your requirements:

- Use **event time** when accuracy and consistency matter most, and your data has timestamps.
- Use **processing time** when you want the simplest implementation and can tolerate approximate results.
- Use **ingestion time** as a middle ground when your data doesn't have timestamps but you need better consistency than processing time.

Most real-world applications use event time because it provides the most accurate and consistent results.

### Windows (Tumbling, Sliding, Session)

Streams are infinite by nature, but many computations need to work on finite chunks of data (e.g., calculating averages, finding maximums). Windows solve this problem by dividing the continuous stream into "buckets" of finite size.

**Window Types**

Flink supports several window types, each suited for different use cases:

**Tumbling Windows: Non-overlapping, Fixed-Size**

Tumbling windows divide the stream into fixed-size, non-overlapping time intervals. Every element belongs to exactly one window.

For example, tumbling windows of 1 hour would create windows like:

- 00:00 - 01:00
- 01:00 - 02:00
- 02:00 - 03:00

These are perfect for calculations like "hourly temperature averages" or "page views per minute."

```python
# Tumbling window example (1 minute)
stream.key_by(lambda x: x['sensor_id']) \
      .window(TumblingEventTimeWindows.of(Time.minutes(1))) \
      .aggregate(...)
```

**Sliding Windows: Overlapping, Fixed-Size**

Sliding windows are fixed-size windows that overlap. They're defined by two parameters: window size and slide interval.

For example, 1-hour windows sliding every 15 minutes would create:

- 00:00 - 01:00
- 00:15 - 01:15
- 00:30 - 01:30
- 00:45 - 01:45
- ...

An element can belong to multiple windows. These are useful for calculations like "average temperature over the last hour, updated every 15 minutes" or "trending topics in the last 24 hours, updated hourly."

```python
# Sliding window example (1 hour windows, sliding every 15 minutes)
stream.key_by(lambda x: x['sensor_id']) \
      .window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(15))) \
      .aggregate(...)
```

**Session Windows: Dynamic, Activity-Based**

Session windows group elements by periods of activity separated by gaps of inactivity. They're defined by a timeout gap parameter.

For example, with a session gap of 30 minutes:

- A session starts when an event arrives
- It continues as long as events keep coming within 30 minutes of each other
- The session ends when no events arrive for 30 minutes

Sessions are ideal for user behavior analysis, such as website visits or app interaction sessions.

```python
# Session window example (30-minute gap)
stream.key_by(lambda x: x['user_id']) \
      .window(EventTimeSessionWindows.withGap(Time.minutes(30))) \
      .aggregate(...)
```

**Global Windows: Custom Triggering**

Global windows assign all elements with the same key to a single window. This is rarely used alone but becomes powerful when combined with custom triggers to determine when to process the window.

For example, you could trigger processing after receiving 100 events or after 1 minute of inactivity.

```python
# Global window with count trigger
stream.key_by(lambda x: x['sensor_id']) \
      .window(GlobalWindows.create()) \
      .trigger(CountTrigger.of(100)) \
      .aggregate(...)
```

**Window Functions**

Once you've defined a window, you need to specify what computation to perform on the elements in each window. Flink provides several functions:

- **Reduce**: Combines elements incrementally using a reducing function
- **Aggregate**: More flexible than reduce, with separate functions for accumulation and result extraction
- **Process**: Most powerful, giving access to metadata about the window and allowing for complex custom logic

**Watermarks and Late Events**

With event time windows, Flink needs to know when it can consider a window complete, even if some events might arrive late. This is done using watermarks, which are special markers in the stream that indicate "no more events with timestamps earlier than X will arrive."

Watermarks help Flink balance between waiting for late events and producing timely results. You can also configure how to handle events that arrive after their window has been processed (e.g., discard them, update the result, or send them to a side output).

### State Management

State refers to information that is remembered across multiple events. In many real-world applications, you need to maintain state to implement your business logic. Examples include:

- Running counters and aggregates
- Windows of recent events
- Machine learning models being updated with new data
- User sessions and activity histories

**Why State Management Matters**

In a distributed system like Flink, managing state is challenging:

- State must be accessible to the relevant tasks
- State must be recoverable if failures occur
- State must be efficient to access and update
- State might be larger than memory

Flink addresses these challenges with its sophisticated state management capabilities.

**Types of State**

Flink provides different types of state abstractions:

**Keyed State vs. Operator State**

- **Keyed State**: State that is partitioned by key. Each key has its own state that is accessible only when processing elements with that key. This is like having a separate counter for each user or device.
- **Operator State**: State that belongs to an operator or function as a whole. It's not partitioned by key but can be distributed when operators scale. Examples include the offsets of a Kafka consumer.

Most applications use keyed state, as it's more common to need state partitioned by some business entity like user ID, device ID, or transaction ID.

**State Primitives**

For keyed state, Flink provides several state primitives:

- **ValueState**: Stores a single value per key. Perfect for counters, latest values, or flags.

  ```python
  # ValueState example
  temp_state = runtime_context.get_state(ValueStateDescriptor("temperature", Types.DOUBLE()))
  current_temp = temp_state.value()  # Get
  temp_state.update(new_temp)  # Set
  ```

- **ListState**: Stores a list of elements per key. Useful for storing recent events or multiple values.

  ```python
  # ListState example
  temps_state = runtime_context.get_list_state(ListStateDescriptor("recent-temps", Types.DOUBLE()))
  temps_state.add(new_temp)  # Add a value
  recent_temps = [temp for temp in temps_state.get()]  # Get all values
  ```

- **MapState**: Stores key-value pairs per key. Like a dictionary for each key.

  ```python
  # MapState example
  device_temps = runtime_context.get_map_state(MapStateDescriptor("device-temps", Types.STRING(), Types.DOUBLE()))
  device_temps.put("living-room", 22.5)
  kitchen_temp = device_temps.get("kitchen")
  ```

- **ReducingState**/**AggregatingState**: Automatically aggregates values using a reduce or aggregate function.

**State Backends**

Flink offers different state backends that determine how and where state is stored:

- **MemoryStateBackend**: Stores state in Java heap memory. Fast but limited by available memory and not durable. Good for development and testing.
- **FsStateBackend**: Stores checkpoints in a file system (local, HDFS, S3, etc.) but keeps working state in memory. Good for production deployments with moderate state size.
- **RocksDBStateBackend**: Stores state in RocksDB, an embedded key-value store. It can handle state larger than available memory by spilling to disk. Slower than in-memory solutions but more scalable.

```python
# Configuring a state backend
env.set_state_backend("rocksdb")
env.get_checkpoint_config().set_checkpoint_storage_uri("hdfs://namenode:8020/checkpoints")
```

The choice of state backend depends on your requirements:

- Small state, high performance → MemoryStateBackend
- Medium state, good performance → FsStateBackend
- Large state, memory constraints → RocksDBStateBackend

### Checkpointing and Fault Tolerance

In a distributed system, failures are inevitable. Machines crash, networks partition, and processes die. Flink's fault tolerance mechanism ensures that despite these failures, the application's state and results remain consistent.

**How Checkpointing Works**

Flink achieves fault tolerance through a mechanism called checkpointing, which periodically takes consistent snapshots of the application's state and the position in the input streams. If a failure occurs, Flink can restart the application from the most recent checkpoint.

Here's the checkpointing process:

1. The JobManager initiates a checkpoint by sending checkpoint barriers to the source operators.
2. These barriers flow through the dataflow graph along with the data.
3. When an operator receives a barrier, it takes a snapshot of its current state.
4. After an operator finishes its checkpoint, it forwards the barrier to downstream operators.
5. Once all operators have completed their checkpoints, the entire checkpoint is complete.

Imagine taking photos of every station in an assembly line at the same logical point in the workflow. If the assembly line stops, you can restart it from exactly where it left off by looking at these photos.

**Exactly-Once vs. At-Least-Once Processing**

Flink supports different processing guarantees:

- **Exactly-Once**: Each event affects the state and is reflected in the output exactly once, even if failures occur. This is the strongest guarantee and is the default in Flink.
- **At-Least-Once**: Each event is guaranteed to be processed at least once, but possibly more than once in case of failures. This can be faster but may lead to duplicate processing.

For many applications, exactly-once semantics are crucial to ensure correct results, especially in domains like finance, billing, or analytics where duplicate or missing events would lead to incorrect outcomes.

**Configuring Checkpointing**

Checkpointing is configurable to balance between fault tolerance overhead and recovery time:

```python
# Basic checkpointing configuration
env.enable_checkpointing(60000)  # Checkpoint every 60 seconds

# Advanced configuration
env.get_checkpoint_config().set_checkpoint_timeout(30000)  # 30 seconds timeout
env.get_checkpoint_config().set_min_pause_between_checkpoints(5000)  # 5 seconds min gap
env.get_checkpoint_config().set_max_concurrent_checkpoints(1)  # Allow only one checkpoint at a time
```

**Savepoints: Manually Triggered Checkpoints**

In addition to automatic checkpointing, Flink supports savepoints, which are manually triggered checkpoints. Savepoints are useful for:

- Application upgrades and maintenance
- A/B testing different versions
- Migrating applications to a different cluster
- Starting from a known good state for debugging

Unlike checkpoints (which may be automatically discarded), savepoints are retained until explicitly deleted.

**Restart Strategies**

When a failure occurs, Flink needs to decide how to restart the application. Several restart strategies are available:

- **Fixed Delay**: Retry a fixed number of times with a delay between attempts
- **Failure Rate**: Restart as long as the failure rate is below a threshold
- **No Restart**: Don't restart on failure (useful for testing)

```python
# Configure restart strategy
env.set_restart_strategy(RestartStrategies.fixed_delay_restart(
    3,  # number of restart attempts
    Time.seconds(10)  # delay between attempts
))
```

Together, checkpointing and restart strategies form a robust fault tolerance system that makes Flink reliable even in the face of failures.

## Summary of Core Concepts

Apache Flink provides a powerful framework for stream processing with:

- **Streams and transformations** as the basic building blocks for data processing pipelines
- **Time semantics** that support different notions of time for accurate processing
- **Windows** to group and process infinite streams in finite chunks
- **State management** for maintaining information across events
- **Checkpointing and fault tolerance** to ensure reliable processing even when failures occur

These concepts work together to enable sophisticated stream processing applications that can handle high throughput, maintain low latency, and provide exactly-once processing guarantees.

---

---

# Setting Up Your Environment

## Understanding Our Docker Environment

Looking at the Docker Compose file you shared, you've set up a comprehensive big data environment that includes Apache Flink along with several other services like Hadoop, Kafka, and more. Let's focus specifically on the Flink-related components:

```yaml
flink-jobmanager:
  image: flink:1.18-scala_2.12
  container_name: flink-jobmanager
  ports:
    - "8085:8081"  # Host port 8085 maps to container port 8081
  command: jobmanager
  environment:
    - |
      FLINK_PROPERTIES=
      jobmanager.rpc.address: flink-jobmanager
  networks:
    net_pet:
      ipv4_address: 172.27.1.25

flink-taskmanager:
  image: flink:1.18-scala_2.12
  container_name: flink-taskmanager
  depends_on:
    - flink-jobmanager
  command: taskmanager
  environment:
    - |
      FLINK_PROPERTIES=
      jobmanager.rpc.address: flink-jobmanager
  networks:
    net_pet:
      ipv4_address: 172.27.1.26
```

This setup creates two main Flink components:

1. **JobManager** (the coordinator) - This is the brain of Flink that manages the execution of your Flink applications. It distributes work, coordinates checkpoints, and handles failures.
2. **TaskManager** (the worker) - This is where the actual processing happens. The TaskManager executes the tasks that make up your Flink job and manages memory and CPU resources.

Your Docker environment also includes Kafka (on port 9092), which we'll use later to create streaming data sources.

To start your Docker environment if it's not already running:

```bash
docker-compose up -d
```

To verify everything is running:

```bash
docker ps | grep flink
```

You should see two containers: `flink-jobmanager` and `flink-taskmanager`.

## Accessing the Flink Dashboard

One of the great things about Flink is its web dashboard, which lets you monitor your jobs and cluster. With your setup, the dashboard is available at:

```
http://localhost:8085
```

When you open this URL in your browser, you'll see the Flink dashboard with several sections:

- **Overview**: Shows a summary of your cluster, including available task slots and running jobs
- **Running Jobs**: Lists all your currently running Flink applications
- **Completed Jobs**: Shows jobs that have finished (successfully or with errors)
- **Task Managers**: Provides details about your worker nodes

Take some time to explore this interface – it will be invaluable for monitoring and debugging your Flink applications.

## Setting Up PyFlink

For our tutorial, we'll use PyFlink, which is the Python API for Apache Flink. It lets you write Flink applications in Python instead of Java or Scala.

Let's create a simple script to test if PyFlink is properly set up in our environment:

1. Create a file called `test_pyflink.py` on your local machine with this content:

```python
# test_pyflink.py
from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import StreamTableEnvironment, EnvironmentSettings

def test_pyflink():
    # Create a StreamExecutionEnvironment
    env = StreamExecutionEnvironment.get_execution_environment()
    env.set_parallelism(1)
    
    # Create a StreamTableEnvironment
    settings = EnvironmentSettings.new_instance().in_streaming_mode().build()
    table_env = StreamTableEnvironment.create(env, settings)
    
    # Print environment information
    print("PyFlink environment is working!")
    
    # Get PyFlink version
    print("PyFlink version:", table_env.get_config().get_configuration().get_string("flink.version", "Unknown"))
    
    # Create a simple table to test functionality
    table_env.execute_sql("""
        CREATE TABLE test_source (
            id BIGINT,
            data STRING
        ) WITH (
            'connector' = 'datagen',
            'rows-per-second' = '5',
            'fields.id.kind' = 'sequence',
            'fields.id.start' = '1',
            'fields.id.end' = '10',
            'fields.data.length' = '10'
        )
    """)
    
    # Test query execution
    result = table_env.execute_sql("SELECT * FROM test_source")
    print("Table API is working. Test query created successfully.")
    
if __name__ == "__main__":
    test_pyflink()
```

1. Copy this file to the Flink JobManager container:

```bash
docker cp test_pyflink.py flink-jobmanager:/opt/
```

1. Run the script in the container:

```bash
docker exec -it flink-jobmanager bash -c "cd /opt && python test_pyflink.py"
```

If PyFlink is set up correctly, you should see the message "PyFlink environment is working!".

Now that we've confirmed PyFlink is working, we're ready to create our first Flink application!

---



## Hands-On: Your First Flink Application



Let’s build and run a simple file-based word-count job using your updated `word_count.py`. We’ll walk through each step—from preparing the files, to submitting the job via the Flink CLI, to inspecting the output in the TaskManager logs.

------

### 1. Prepare Your Code and Input

1. **Create** `word_count.py` locally with the following content:

   ```python
   # word_count.py
   from pyflink.datastream import StreamExecutionEnvironment
   from pyflink.common.typeinfo import Types
   import os
   
   def word_count():
       # Set up the execution environment
       env = StreamExecutionEnvironment.get_execution_environment()
       
       # Read the input file
       input_file = "/opt/input.txt"
       
       # Check if file exists
       if not os.path.exists(input_file):
           print(f"Error: Input file {input_file} not found!")
           return
       
       # Read file content
       with open(input_file, "r") as file:
           lines = file.readlines()
       
       # Create a data stream from the collection
       data_stream = env.from_collection(lines)
       
       # Process the data
       # For flink run -py, we use lambda functions instead of custom classes
       # This avoids issues with Python UDF serialization
       counts = data_stream \
           .flat_map(lambda line: [(word.lower(), 1) for word in line.split() if word.strip()],
                     output_type=Types.TUPLE([Types.STRING(), Types.INT()])) \
           .key_by(lambda x: x[0]) \
           .sum(1)
       
       # Print the results
       counts.print()
       
       # Execute the job
       env.execute("PyFlink File Word Count")
   
   if __name__ == "__main__":
       word_count()
   ```

2. **Create** a simple text file, `input.txt`, with some sample lines:

   ```bash
   echo -e "Hello Flink\nHello world\nFlink streaming example" > input.txt
   ```

------

### 2. Copy Files into the Flink JobManager

Use Docker to push both `word_count.py` and `input.txt` into the `/opt` directory of your `flink-jobmanager` container:

```bash
docker cp datasets/input.txt flink-jobmanager:/opt/
docker cp scripts/word_count.py flink-jobmanager:/opt/
```

------

### 3. Submit the Job via the Flink CLI

Run your Python job through Flink so that all required JARs and dependencies are on the classpath:

```bash
docker exec -it flink-jobmanager \
  bash -c "cd /opt && flink run -py word_count.py --parallelism 1"
```

- The `-py` flag tells Flink to treat the file as a PyFlink job.
- `--parallelism 1` ensures we use a single task slot for clear, ordered output.

------

### 4. Inspect the Output in the TaskManager Logs

Because we used `counts.print()`, results are emitted to the TaskManager’s STDOUT. Tail and filter its logs to see your word counts:

```bash
docker logs -f flink-taskmanager
```

Or to only show lines containing “hello” (your sample word):

```bash
docker logs flink-taskmanager | grep -i hello
```

You should see output lines like:

```
(hello,1)
(hello,2)
```

…confirming that “hello” appeared twice across your input lines.

------

### 5. What Just Happened?

1. **Environment setup:** We created a `StreamExecutionEnvironment`.
2. **Data ingestion:** We read `input.txt` into a Python list, then converted it to a Flink collection via `env.from_collection()`.
3. **Transformation:** We split each line into words, lowercased them, and paired each word with `1`.
4. **Keying & aggregation:** We grouped by the word (`key_by`) and summed counts.
5. **Sink:** We printed results to STDOUT, which Flink captured in the TaskManager logs.
6. **Execution:** We launched the job via `flink run -py`, ensuring all JVM dependencies were loaded.

------

You’ve now successfully run your first PyFlink application inside your Docker-Compose cluster! Next up, you can extend this to real streams—reading from Kafka, windowing counts over time, and writing results back to Kafka or external storage.



---

