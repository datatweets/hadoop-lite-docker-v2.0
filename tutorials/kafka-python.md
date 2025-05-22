# Working with Kafka using Python in Docker

This tutorial will take you through the complete process of setting up and running Kafka producers and consumers using Python in a Docker environment. I'll cover everything from installing dependencies to running the code and understanding the results.

## Prerequisites

Before starting, ensure you have:

- Docker and Docker Compose installed on your local machine
- A running Kafka environment in Docker (based on your docker-compose.yaml)
- VS Code or another text editor for local development
- Basic understanding of Python and Kafka concepts

## Step 1: Verifying Your Kafka Environment

First, let's ensure your Kafka environment is properly set up and running.

### Check If Kafka Container Is Running

```bash
docker ps | grep kafka
```

You should see your Kafka container in the list of running containers.

### Verify Kafka Network Connectivity

Test if your Jupyter container can communicate with Kafka:

```bash
docker exec -it jupyter-spark ping kafka
```

This command attempts to ping the Kafka container from your Jupyter container. You should see successful ping responses, indicating network connectivity between the containers.

### Check If Kafka Is Listening on the Expected Port

Verify that Kafka is correctly listening on port 9092:

```bash
docker exec -it kafka netstat -tulpn | grep 9092
```

This command shows all network ports that Kafka is listening on. You should see port 9092 in the output, which is the standard port for Kafka.

## Step 2: Installing Required Python Packages

The confluent-kafka Python package is needed to interact with Kafka. Since your environment has limitations with package installations, we'll use a binary installation approach:

```bash
docker exec -it jupyter-spark /bin/bash pip install --only-binary :all: confluent-kafka
```

This command installs the confluent-kafka package using only pre-compiled binary packages, avoiding the need to compile from source, which would require additional development tools.

## Step 3: Setting Up Your Local Development Environment

Now, create a directory structure on your local machine to organize your Kafka code:

```bash
# Create a directory for your Kafka Python code
mkdir -p base/kafka-scripts/
```

This directory will store our Python scripts before we copy them to the Docker container.

## Step 4: Creating the Kafka Topic

Before sending messages, we need to create a Kafka topic. Run the following command:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic test-topic \
  --partitions 3 \
  --replication-factor 1
```

This command creates a new topic named "test-topic" with 3 partitions and a replication factor of 1. The partitions allow for parallel processing of messages, while the replication factor determines how many copies of the data are maintained (1 is appropriate for a single-broker setup).

### Verify Topic Creation

To ensure the topic was successfully created, list all topics:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --list \
  --bootstrap-server localhost:9092
```

You should see "test-topic" in the list of available topics.

## Step 5: Creating the Kafka Producer Script

Open VS Code and create a new file called `kafka-producer.py` in the `base/kafka-scripts` directory with the following content:

```python
from confluent_kafka import Producer
import json
import time

def delivery_report(err, msg):
    """Called once for each message produced to indicate delivery result."""
    if err is not None:
        print(f'Message delivery failed: {err}')
    else:
        print(f'Message {msg.key().decode("utf-8")} sent to {msg.topic()} partition {msg.partition()}')

# Configure the producer
conf = {
    'bootstrap.servers': 'kafka:9092',  # Use the service name from docker-compose
}

# Create Producer instance
producer = Producer(conf)

# Define the topic
topic = 'test-topic'

# Produce 5 test messages
for i in range(5):
    # Create message data
    data = {
        'message_id': i,
        'timestamp': time.time(),
        'content': f'Test message {i}'
    }
    
    # Convert the data to JSON and encode as UTF-8
    message_value = json.dumps(data).encode('utf-8')
    
    # Produce the message
    producer.produce(
        topic=topic,
        key=str(i).encode('utf-8'),
        value=message_value,
        callback=delivery_report
    )
    
    # Trigger any available delivery report callbacks
    producer.poll(0)
    
    # Add a small delay between messages
    time.sleep(1)

# Wait for any outstanding messages to be delivered
producer.flush()
print('All messages sent!')
```

### Key Elements of the Producer Code

1. **Kafka Configuration**: The `bootstrap.servers` parameter is set to 'kafka:9092', using the service name defined in your docker-compose file. This allows the container to find the Kafka broker on your Docker network.
2. **Delivery Callback**: The `delivery_report` function is called for each produced message, providing feedback on successful delivery or errors.
3. **Message Format**: Each message includes a structured JSON payload with an ID, timestamp, and content.
4. **Partitioning Strategy**: By providing a key (the message ID), we ensure that messages with the same key always go to the same partition, which maintains order for related messages.
5. **Asynchronous Production**: Messages are produced asynchronously, with `producer.poll(0)` triggering delivery callbacks, and `producer.flush()` ensuring all messages are sent before the program ends.

## Step 6: Creating the Kafka Consumer Script

Next, create another file called `kafka-consumer.py` in the same directory:

```python
from confluent_kafka import Consumer, KafkaError
import json

# Configure the consumer
conf = {
    'bootstrap.servers': 'kafka:9092',
    'group.id': 'python-consumer-group',
    'auto.offset.reset': 'earliest'  # Start reading from the beginning
}

# Create Consumer instance
consumer = Consumer(conf)

# Subscribe to our test topic
consumer.subscribe(['test-topic'])

# Process messages
try:
    print('Waiting for messages...')
    message_count = 0
    max_messages = 10

    # Set a timeout for the consumer
    consumer.config['consumer_timeout_ms'] = 30000  # 30 seconds

    # Read messages
    for message in consumer:
        message_count += 1
        
        # Parse the message value (assuming JSON)
        message_value = json.loads(message.value().decode('utf-8'))
        
        # Display the message
        print(f'Received message {message_count}: {message_value}')
        
        # Stop after max_messages
        if message_count >= max_messages:
            break
            
except KeyboardInterrupt:
    print('Consumer stopped by user')
except Exception as e:
    print(f'Error consuming messages: {e}')
finally:
    # Close the consumer connection
    consumer.close()
    print(f'Consumer closed, received {{message_count}} messages')
```

### Key Elements of the Consumer Code

1. **Consumer Group**: The `group.id` parameter assigns the consumer to a specific group. This is crucial for offset management and scalability.
2. **Offset Reset Policy**: The `auto.offset.reset` parameter is set to 'earliest', causing the consumer to start reading from the beginning of the topic if no offset is committed yet.
3. **Error Handling**: The code includes try/except blocks to handle errors gracefully and ensure proper cleanup.
4. **Message Processing**: Each received message is parsed from JSON format and displayed.
5. **Consumer Timeout**: A timeout is set to prevent the consumer from waiting indefinitely if no new messages arrive.

## Step 7: Creating the Destination Directory in the Container

If you haven't already created the directory where you'll store your Python files inside the container, do so now:

```bash
# Connect to the container
docker exec -it jupyter-spark /bin/bash

# Create the directory structure
mkdir -p /mnt/python/kafka
```

This creates a dedicated directory inside the container for your Kafka scripts.

## Step 8: Copying Files to Your Docker Container

Now, copy your Python scripts from your local machine to the Docker container:

```bash
# Copy the producer script
docker cp base/kafka-script/kafka-producer.py jupyter-spark:/mnt/python/kafka/

# Copy the consumer script
docker cp base/kafka-script/kafka-consumer.py jupyter-spark:/mnt/python/kafka/
```

You should see a confirmation message like:

```
Successfully copied 2.56kB to jupyter-spark:/mnt/python/kafka/
```

This step transfers your local code to the container where it can access the Kafka environment.

## Step 9: Verifying File Transfer

Ensure your files were successfully copied:

```bash
docker exec -it jupyter-spark ls -la /mnt/python/kafka/
```

You should see both the producer and consumer Python files listed in the directory.

## Step 10: Running the Producer Script

Now, connect to your container and run the producer script:

```bash
# Connect to the container
docker exec -it jupyter-spark /bin/bash

# Navigate to the directory containing your scripts
cd /mnt/python/kafka

# Run the producer script
python kafka-producer.py
```

You should see output similar to:

```
Message 0 sent to test-topic partition 0
Message 1 sent to test-topic partition 0
Message 2 sent to test-topic partition 2
Message 3 sent to test-topic partition 1
Message 4 sent to test-topic partition 2
All messages sent!
```

### Understanding the Producer Output

The output provides important insights into Kafka's behavior:

1. **Distribution Across Partitions**: Messages are distributed across partitions 0, 1, and 2. This distribution is based on the message key hash.
2. **Deterministic Partitioning**: If you run the producer multiple times with the same keys, you'll notice that each key consistently maps to the same partition. This is because Kafka uses a deterministic hash function to map keys to partitions.
3. **Successful Delivery**: The "All messages sent!" confirmation indicates that all messages were successfully delivered to the Kafka broker.

## Step 11: Running the Consumer Script

In a separate terminal, connect to your container again and run the consumer script:

```bash
# Connect to the container
docker exec -it jupyter-spark /bin/bash

# Navigate to the directory
cd /mnt/python/kafka

# Run the consumer script
python kafka-consumer.py
```

You should see output similar to:

```
Waiting for messages...
Received message 1: {'message_id': 0, 'timestamp': 1747754626.7465222, 'content': 'Test message 0'}
Received message 2: {'message_id': 1, 'timestamp': 1747754627.8723183, 'content': 'Test message 1'}
Received message 3: {'message_id': 2, 'timestamp': 1747754628.8809543, 'content': 'Test message 2'}
Received message 4: {'message_id': 4, 'timestamp': 1747754630.9035134, 'content': 'Test message 4'}
Received message 5: {'message_id': 3, 'timestamp': 1747754629.890043, 'content': 'Test message 3'}
Received message 6: {'message_id': 0, 'timestamp': 1747754711.736713, 'content': 'Test message 0'}
Received message 7: {'message_id': 1, 'timestamp': 1747754712.7470965, 'content': 'Test message 1'}
Received message 8: {'message_id': 2, 'timestamp': 1747754713.7636495, 'content': 'Test message 2'}
Received message 9: {'message_id': 3, 'timestamp': 1747754714.7726336, 'content': 'Test message 3'}
Received message 10: {'message_id': 4, 'timestamp': 1747754715.782456, 'content': 'Test message 4'}
Consumer closed, received {message_count} messages
```

### Understanding the Consumer Output

The consumer output demonstrates several important aspects of Kafka's behavior:

1. **Historical Message Access**: The consumer reads all messages from the beginning of the topic, including those from previous producer runs. This is because we configured `auto.offset.reset=earliest`.
2. **Partition-Based Order**: Notice that messages 3 and 4 might be received out of sequence (message 4 before message 3). This happens because they were on different partitions, and Kafka only guarantees order within a partition, not across partitions.
3. **Message Persistence**: Even though these messages were sent at different times (as evidenced by the timestamps), the consumer can access all of them. This demonstrates Kafka's persistence mechanism, where messages are stored durably and remain available for consumption until their retention period expires.
4. **Structured Data**: The JSON structure of each message is preserved and properly parsed by the consumer, showing how Kafka can be used to transmit structured data between systems.

## Step 12: Advanced Kafka Management (Optional)

For a more in-depth understanding of your Kafka setup, you can use additional commands:

### Describe Topic Details

```bash
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --describe \
  --bootstrap-server localhost:9092 \
  --topic test-topic
```

This shows detailed information about the topic, including its partitions, replica assignments, and leader brokers.

### Monitor Consumer Group

```bash
docker exec -it kafka /opt/kafka/bin/kafka-consumer-groups.sh --describe \
  --bootstrap-server localhost:9092 \
  --group python-consumer-group
```

This displays information about the consumer group's progress, including current offsets, log-end offsets, and consumer lag.

### Use Kafka Console Consumer for Troubleshooting

```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic test-topic \
  --from-beginning
```

This command lets you view messages directly from the command line, which is useful for troubleshooting.

## Understanding Key Kafka Concepts Demonstrated

This exercise demonstrates several fundamental Kafka concepts:

### 1. Message Persistence

Kafka stores messages reliably, allowing consumers to read them long after they were produced. This is why your consumer could access messages from previous producer runs. Kafka retains messages for a configurable period (the retention period), regardless of whether they've been consumed.

### 2. Partitioning

Our topic had 3 partitions, allowing messages to be distributed for parallel processing. When you provide a key, Kafka ensures that messages with the same key always go to the same partition, which guarantees ordering for those related messages. This is crucial for use cases where order matters, such as event sequences for a specific user or entity.

### 3. Consumer Groups

The consumer script used a named consumer group, which allows Kafka to track its position (offset) in each partition. If you ran multiple instances of the consumer with the same group ID, Kafka would automatically distribute the partitions among them, enabling parallel processing.

### 4. Offset Management

Kafka maintains offsets for each consumer group, tracking which messages have been consumed. By setting `auto.offset.reset=earliest`, we instructed the consumer to start from the beginning of the topic if no committed offset was found. In a production environment, consumers typically commit their offsets after successfully processing messages.

### 5. Scalability

The combination of partitioning and consumer groups enables Kafka's scalability. You can add more partitions to a topic to increase throughput, and add more consumers to a group to scale out processing (up to the number of partitions).

## Conclusion

You've successfully set up a complete Kafka environment in Docker and implemented a basic producer and consumer using Python. This foundation can be expanded to build more complex event streaming applications, such as:

- Real-time analytics pipelines
- Event-driven microservices
- Distributed logging systems
- Change data capture (CDC) solutions

The workflow you've established—creating files locally, copying them to a container, and executing them there—is a practical approach that works well with Docker-based development. It separates development from execution, making it easier to iterate on your code while maintaining a consistent runtime environment.

By understanding the core concepts demonstrated in this tutorial, you're well-prepared to leverage Kafka's capabilities for building scalable, resilient, and real-time data streaming applications.
