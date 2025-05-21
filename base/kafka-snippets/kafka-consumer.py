from kafka import KafkaConsumer
import json

# Configure the consumer
consumer = KafkaConsumer(
    'test-topic',  # topic to consume
    bootstrap_servers=['kafka:9092'],
    auto_offset_reset='earliest',  # start at earliest message
    enable_auto_commit=True,
    group_id='my-jupyter-group',
    value_deserializer=lambda x: json.loads(x.decode('utf-8'))
)

# Read 10 messages or wait 30 seconds
print("Waiting for messages...")
message_count = 0
max_messages = 10

# Set a timeout for the consumer
consumer.config['consumer_timeout_ms'] = 30000  # 30 seconds

try:
    for message in consumer:
        message_count += 1
        print(f"Received message {message_count}: {message.value}")
        
        if message_count >= max_messages:
            break
except KeyboardInterrupt:
    print("Interrupted by user")
except Exception as e:
    print(f"Error consuming messages: {e}")
finally:
    # Close the consumer connection
    consumer.close()
    print("Consumer closed, received {message_count} messages")