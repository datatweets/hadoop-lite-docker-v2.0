from kafka import KafkaProducer
import json
import time

# Configure the producer
# Note: 'kafka' is the service name from your docker-compose file
producer = KafkaProducer(
    bootstrap_servers=['kafka:9092'],  
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

# Send 5 test messages
for i in range(5):
    # Create a test message
    message = {
        'message_id': i,
        'timestamp': time.time(),
        'content': f"Test message {i}"
    }
    
    # Send to topic
    future = producer.send('test-topic', message)
    
    # Print result (optional - shows successful delivery)
    try:
        record_metadata = future.get(timeout=10)
        print(f"Message {i} sent to {record_metadata.topic} partition {record_metadata.partition}")
    except Exception as e:
        print(f"Error sending message: {e}")
    
    # Small delay between messages
    time.sleep(1)

# Make sure all messages are sent
producer.flush()
print("All messages sent!")