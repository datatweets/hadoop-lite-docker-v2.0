# Apache Kafka Command Line Tutorial: A Comprehensive Guide

This tutorial will guide you through using Apache Kafka from the command line, covering everything from basic operations to advanced concepts. I'll provide clear explanations and step-by-step instructions for each command, helping you understand not just what to do, but why you're doing it.

## Part 1: Kafka Core Concepts and Command Line Operations

### 1.1 Starting with Kafka CLI

First, let's connect to your Kafka container:

```bash
docker exec -it kafka /bin/bash
```

This command opens an interactive shell inside your Kafka container. From here, you'll run all the Kafka command-line tools.

### 1.2 Topic Management

#### Creating Topics

Topics are the fundamental organizational units in Kafka. Let's create a topic with multiple partitions:

```bash
/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic clickstream_data \
  --partitions 3 \
  --replication-factor 1
```

Command explanation:

- `--create`: Specifies that we're creating a new topic
- `--bootstrap-server`: The Kafka broker to connect to
- `--topic`: The name of the topic to create
- `--partitions`: Number of partitions to divide the topic into
- `--replication-factor`: Number of copies of each partition (limited by broker count)

#### Listing Topics

To see all existing topics:

```bash
/opt/kafka/bin/kafka-topics.sh --list \
  --bootstrap-server localhost:9092
```

This command connects to the Kafka broker and retrieves a list of all topics.

#### Describing Topics

To get detailed information about a specific topic:

```bash
/opt/kafka/bin/kafka-topics.sh --describe \
  --bootstrap-server localhost:9092 \
  --topic clickstream_data
```

This provides information about:

- Partition count and IDs
- Replication factor
- Leader broker for each partition
- In-sync replicas (ISRs)
- Replica assignment

#### Modifying Topics

To add partitions to an existing topic:

```bash
/opt/kafka/bin/kafka-topics.sh --alter \
  --bootstrap-server localhost:9092 \
  --topic clickstream_data \
  --partitions 6
```

This increases the partition count from 3 to 6. Note that you can only increase partitions, never decrease them.

#### Deleting Topics

To delete a topic:

```bash
/opt/kafka/bin/kafka-topics.sh --delete \
  --bootstrap-server localhost:9092 \
  --topic clickstream_data
```

Note: For deletion to work, the broker must have `delete.topic.enable=true` configured.

### 1.3 Understanding Partitioning and Replication

Partitioning and replication are foundational concepts in Kafka:

#### Partitioning

Let's create a topic to demonstrate partitioning:

```bash
/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic partitioning_demo \
  --partitions 4 \
  --replication-factor 1
```

We've created a topic with 4 partitions. When messages are sent to this topic, they'll be distributed across these partitions based on:

1. The message key hash (if a key is provided)
2. Round-robin distribution (if no key is provided)

Checking partition distribution:

```bash
/opt/kafka/bin/kafka-topics.sh --describe \
  --bootstrap-server localhost:9092 \
  --topic partitioning_demo
```

The output shows you the 4 partitions and their leaders. In your single-broker setup, all leaders will be on broker 0.

### 1.4 Producing Messages

Let's use the console producer to send messages:

#### Basic Producer

```bash
/opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic partitioning_demo
```

After running this command, you'll get a prompt where you can type messages. Each line you type becomes a message sent to the topic. Press Ctrl+D when you're done.

#### Producer with Message Keys

To send messages with keys (ensuring related messages go to the same partition):

```bash
/opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic partitioning_demo \
  --property "parse.key=true" \
  --property "key.separator=:"
```

Now you can enter messages with keys in this format: `key:value`. For example:

```
user1:login event
user1:view homepage
user2:login event
user1:search products
```

Messages with the same key (e.g., "user1") will go to the same partition, ensuring ordered processing for that key.

### 1.5 Consuming Messages

Now let's retrieve messages from our topics:

#### Basic Consumer

```bash
/opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic partitioning_demo \
  --from-beginning
```

This command:

- Connects to the Kafka broker
- Reads all messages from the topic from the beginning
- Prints them to the console

Without `--from-beginning`, you would only see new messages that arrive after starting the consumer.

#### Consumer with Keys

To see both keys and values:

```bash
/opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic partitioning_demo \
  --from-beginning \
  --property "print.key=true" \
  --property "key.separator=:"
```

This displays both the key and value of each message, separated by a colon.

#### Consumer Groups

Consumer groups allow multiple consumers to divide the work of processing messages. Let's create a consumer in a specific group:

```bash
/opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic partitioning_demo \
  --group demo-group-1
```

Run this command in multiple terminals to see how consumers in the same group divide the partitions among themselves. Each consumer will get a subset of partitions, and thus only a subset of the messages.

#### Consumer Group Management

To list all consumer groups:

```bash
/opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --list
```

To describe a specific consumer group:

```bash
/opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group demo-group-1
```

This shows:

- Partition assignments
- Current offsets
- Log-end offsets
- Consumer lag (the difference between the log-end offset and current offset)

### 1.6 Producer/Consumer Configuration

#### Producer Configuration

Let's examine key producer configurations using command-line properties:

```bash
/opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic partitioning_demo \
  --producer-property acks=all \
  --producer-property batch.size=16384 \
  --producer-property linger.ms=100
```

Configuration explained:

- `acks=all`: Wait for all replicas to acknowledge the message (maximum durability)
- `batch.size=16384`: Group messages into 16KB batches before sending
- `linger.ms=100`: Wait up to 100ms to form batches

#### Consumer Configuration

Similarly for consumers:

```bash
/opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic partitioning_demo \
  --group test-group \
  --consumer-property auto.offset.reset=earliest \
  --consumer-property enable.auto.commit=false
```

Configuration explained:

- `auto.offset.reset=earliest`: Start reading from the beginning if no offset is found
- `enable.auto.commit=false`: Don't automatically commit offsets (manual control)

### 1.7 Exactly-Once Semantics

Exactly-once semantics ensures messages are processed exactly once, with no duplicates or losses. In the command line, we can demonstrate the principles:

#### Reset Consumer Group Offsets

To simulate reprocessing without duplicates, you can reset offsets:

```bash
/opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group demo-group-1 \
  --topic partitioning_demo \
  --reset-offsets --to-earliest --execute
```

This resets the consumer group to start from the beginning, but when consumers read, they track which messages they've processed, avoiding duplicates.

## Part 2: Hands-on: Clickstream Analysis Pipeline

Let's create a practical pipeline for analyzing clickstream data:

### 2.1 Setting Up the Topics

```bash
# Create raw data topic
/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic clickstream_raw \
  --partitions 3 \
  --replication-factor 1

# Create processed data topic
/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic clickstream_processed \
  --partitions 3 \
  --replication-factor 1

# Create analytics results topic
/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic clickstream_analytics \
  --partitions 1 \
  --replication-factor 1
```

We've created three topics:

1. `clickstream_raw`: Holds raw clickstream events
2. `clickstream_processed`: Holds filtered and enriched events
3. `clickstream_analytics`: Holds aggregated analytics results

### 2.2 Generating Sample Clickstream Data

Let's create a file with sample clickstream data. Exit your Kafka container temporarily and create a file on your host:

```bash
exit  # Exit the Kafka container
```

Create a file called `clickstream_sample.json` with some sample events:

```bash
cat > clickstream_sample.json << 'EOL'
{"user_id": "user123", "timestamp": "2025-05-20T10:00:00", "page": "/home", "action": "view"}
{"user_id": "user123", "timestamp": "2025-05-20T10:01:30", "page": "/products", "action": "view"}
{"user_id": "user123", "timestamp": "2025-05-20T10:02:15", "page": "/products/item123", "action": "click"}
{"user_id": "user123", "timestamp": "2025-05-20T10:03:00", "page": "/cart", "action": "view"}
{"user_id": "user123", "timestamp": "2025-05-20T10:04:20", "page": "/checkout", "action": "view"}
{"user_id": "user456", "timestamp": "2025-05-20T10:01:00", "page": "/home", "action": "view"}
{"user_id": "user456", "timestamp": "2025-05-20T10:02:00", "page": "/about", "action": "view"}
{"user_id": "user789", "timestamp": "2025-05-20T10:00:30", "page": "/home", "action": "view"}
{"user_id": "user789", "timestamp": "2025-05-20T10:01:45", "page": "/products", "action": "view"}
{"user_id": "user789", "timestamp": "2025-05-20T10:03:10", "page": "/products/item456", "action": "click"}
EOL
```

Copy this file to the Kafka container:

```bash
docker cp clickstream_sample.json kafka:/tmp/
```

Now reconnect to your Kafka container:

```bash
docker exec -it kafka /bin/bash
```

### 2.3 Producing Clickstream Events

Now let's send these events to the `clickstream_raw` topic:

```bash
cat /tmp/clickstream_sample.json | /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic clickstream_raw
```

This reads the JSON file line by line and sends each line as a message to the topic.

### 2.4 Processing with Stream Processing

In a real scenario, we'd use Kafka Streams or a similar framework for processing. For demonstration purposes, we'll use the console consumer to read from one topic and the producer to write to another:

```bash
# In one terminal - read from raw and filter
/opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic clickstream_raw \
  --from-beginning
```

Imagine we're filtering for only "click" actions. Note down those events, and in another terminal, produce them to the processed topic:

```bash
# Connect to the Kafka container in a new terminal
docker exec -it kafka /bin/bash

# Now produce filtered events
/opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic clickstream_processed
```

Enter the click events (modify slightly to indicate processing):

```
{"user_id": "user123", "timestamp": "2025-05-20T10:02:15", "page": "/products/item123", "action": "click", "processed": true}
{"user_id": "user789", "timestamp": "2025-05-20T10:03:10", "page": "/products/item456", "action": "click", "processed": true}
```

### 2.5 Aggregating Analytics

Finally, let's simulate an analytics aggregation. In a real stream processing application, we'd count events by page, user, etc.:

```bash
# In a new terminal, produce analytics results
docker exec -it kafka /bin/bash

/opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic clickstream_analytics
```

Enter aggregated data:

```
{"timestamp": "2025-05-20T10:05:00", "metrics": {"page_views": {"home": 3, "products": 2, "cart": 1, "checkout": 1, "about": 1}, "clicks": {"products/item123": 1, "products/item456": 1}}}
```

### 2.6 Consuming Analytics Results

```bash
/opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic clickstream_analytics \
  --from-beginning
```

This pipeline demonstrates the typical flow of a clickstream analysis system:

1. Raw events are ingested into Kafka
2. Stream processing filters and transforms the events
3. Aggregations are performed and results stored for downstream applications

## Part 3: Kafka Streams API Concepts

While we can't directly use the Kafka Streams API from the command line, we can understand its concepts:

### 3.1 KStream vs KTable

Let's demonstrate the difference with topics:

```bash
# Create topics representing a KStream and a KTable
/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic user_clicks_stream \
  --partitions 3 \
  --replication-factor 1

/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic user_profiles_table \
  --partitions 3 \
  --replication-factor 1
```

Now let's produce messages in a pattern that illustrates the difference:

```bash
# Stream-like data (each message is a new event)
/opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic user_clicks_stream \
  --property "parse.key=true" \
  --property "key.separator=:"
```

Enter:

```
user1:{"page": "home", "timestamp": "10:00"}
user1:{"page": "products", "timestamp": "10:01"}
user2:{"page": "home", "timestamp": "10:00"}
user1:{"page": "cart", "timestamp": "10:02"}
```

Each message represents a separate event. Now for table-like data:

```bash
/opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic user_profiles_table \
  --property "parse.key=true" \
  --property "key.separator=:"
```

Enter:

```
user1:{"name": "John", "age": 30}
user2:{"name": "Alice", "age": 25}
user1:{"name": "John", "age": 31}
```

In a KTable, the last entry for each key represents the current state. So user1's current state is age 31, not 30.

### 3.2 State Storage Management

Kafka Streams uses state stores to manage state. From the command line, we can look at the internal topics it creates:

```bash
/opt/kafka/bin/kafka-topics.sh --list \
  --bootstrap-server localhost:9092 | grep -E '(changelog|repartition)'
```

If you've run Kafka Streams applications, you might see topics like:

- `app-name-store-name-changelog`: Records changes to state stores
- `app-name-repartition`: Used for repartitioning data

### 3.3 Interactive Queries

Interactive queries let you access state directly in a Kafka Streams application. While this isn't accessible from the command line, we can set up topics that would be used by such a system:

```bash
/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic page_view_counts \
  --partitions 3 \
  --replication-factor 1
```

## Part 4: Hands-on: Real-time Aggregations

For our final example, let's simulate real-time aggregations:

### 4.1 Create Topics for Real-time Analytics

```bash
# Create input topic
/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic sales_transactions \
  --partitions 3 \
  --replication-factor 1

# Create output topic
/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic sales_aggregations \
  --partitions 1 \
  --replication-factor 1
```

### 4.2 Produce Sample Sales Data

```bash
/opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic sales_transactions \
  --property "parse.key=true" \
  --property "key.separator=:"
```

Enter data with product IDs as keys:

```
product1:{"amount": 100.0, "quantity": 2, "timestamp": "2025-05-20T10:00:00"}
product2:{"amount": 50.0, "quantity": 1, "timestamp": "2025-05-20T10:01:00"}
product1:{"amount": 75.0, "quantity": 1, "timestamp": "2025-05-20T10:02:00"}
product3:{"amount": 200.0, "quantity": 1, "timestamp": "2025-05-20T10:03:00"}
product2:{"amount": 150.0, "quantity": 3, "timestamp": "2025-05-20T10:04:00"}
```

### 4.3 Produce Aggregated Results

In a real application, Kafka Streams would compute these aggregations. We'll simulate the output:

```bash
/opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic sales_aggregations
```

Enter aggregated data:

```
{"timestamp": "2025-05-20T10:05:00", "window_size": "5min", "aggregations": {"product1": {"total_sales": 175.0, "total_quantity": 3}, "product2": {"total_sales": 200.0, "total_quantity": 4}, "product3": {"total_sales": 200.0, "total_quantity": 1}}}
```

### 4.4 View Aggregation Results

```bash
/opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic sales_aggregations \
  --from-beginning
```

This simulates a real-time aggregation pipeline where:

1. Individual transactions flow into the system
2. Stream processing aggregates them by key (product ID)
3. Windowed results are produced showing totals for each product

## Advanced Kafka Operations

### Log Inspection and Maintenance

Viewing partition segments:

```bash
/opt/kafka/bin/kafka-dump-log.sh \
  --files /kafka/kafka-logs-kafka/clickstream_raw-0/00000000000000000000.log \
  --print-data-log
```

This shows the raw contents of a log segment file (replace the path with your actual log file).

### Monitoring Consumer Lag

```bash
/opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group demo-group-1
```

Watch the "LAG" column to see how far behind consumers are.

### Testing Producer Performance

```bash
/opt/kafka/bin/kafka-producer-perf-test.sh \
  --topic test-perf \
  --num-records 100000 \
  --record-size 1000 \
  --throughput 10000 \
  --producer-props bootstrap.servers=localhost:9092
```

This produces 100,000 records of 1KB each at a maximum rate of 10,000 messages per second.

### Testing Consumer Performance

```bash
/opt/kafka/bin/kafka-consumer-perf-test.sh \
  --bootstrap-server localhost:9092 \
  --topic test-perf \
  --messages 100000 \
  --group perf-test-group
```

This measures how quickly a consumer can read 100,000 messages from the topic.

## Conclusion

This comprehensive tutorial has covered Kafka's command-line operations, from basic topic management to complex stream processing concepts. You've learned how to:

1. Manage Kafka topics, including creation, modification, and deletion
2. Understand partitioning and replication through practical examples
3. Configure producers and consumers for different reliability and performance needs
4. Set up and interact with a complete data pipeline for clickstream analysis
5. Understand Kafka Streams concepts like KStreams and KTables
6. Work with real-time aggregations and analytics

These operations form the foundation for building scalable, reliable streaming data applications with Apache Kafka.

Remember that while the command line is excellent for learning and administration, production applications typically use client libraries in languages like Java, Python, or Go to interact with Kafka. The concepts you've learned here apply directly to those programming interfaces as well.