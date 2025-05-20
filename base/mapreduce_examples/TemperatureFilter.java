import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TemperatureFilter {

  public static class TempMapper
       extends Mapper<Object, Text, Text, NullWritable> {

    private final static float THRESHOLD = 30.0f;
    
    @Override
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      // Parse CSV: station,date,temperature
      String line = value.toString();
      String[] parts = line.split(",");
      
      if (parts.length == 3) {
        try {
          float temp = Float.parseFloat(parts[2]);
          
          // Filter temperatures above threshold
          if (temp > THRESHOLD) {
            context.write(value, NullWritable.get());
          }
        } catch (NumberFormatException e) {
          // Skip lines with invalid temperatures
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "temperature filter");
    job.setJarByClass(TemperatureFilter.class);
    job.setMapperClass(TempMapper.class);
    
    // This job is mapper-only (no reducer needed)
    job.setNumReduceTasks(0);
    
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(NullWritable.class);
    
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}