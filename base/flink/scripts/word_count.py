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