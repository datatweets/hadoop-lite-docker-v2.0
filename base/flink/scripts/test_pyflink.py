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