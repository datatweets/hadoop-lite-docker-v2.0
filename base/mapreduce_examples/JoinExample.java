import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class JoinExample {

  public static class JoinMapper
       extends Mapper<Object, Text, Text, Text> {
    
    private Text outKey = new Text();
    private Text outValue = new Text();
    
    @Override
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      // Get file name to determine data source
      String filename = ((FileSplit) context.getInputSplit()).getPath().getName();
      String line = value.toString();
      
      // Process employee data: id,name,dept_id
      if (filename.startsWith("employees")) {
        String[] parts = line.split(",");
        if (parts.length == 3) {
          String deptId = parts[2];
          
          // Key is the department ID for joining
          outKey.set(deptId);
          
          // Value is tagged with 'E:' prefix for employee data
          outValue.set("E:" + parts[0] + "," + parts[1]);
          context.write(outKey, outValue);
        }
      } 
      // Process department data: id,name
      else if (filename.startsWith("departments")) {
        String[] parts = line.split(",");
        if (parts.length == 2) {
          String deptId = parts[0];
          
          // Key is the department ID for joining
          outKey.set(deptId);
          
          // Value is tagged with 'D:' prefix for department data
          outValue.set("D:" + parts[1]);
          context.write(outKey, outValue);
        }
      }
    }
  }
  
  public static class JoinReducer
       extends Reducer<Text, Text, Text, Text> {
    
    private Text outValue = new Text();
    
    @Override
    public void reduce(Text key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {
      
      // Store department name
      String deptName = "";
      
      // Store employee details for this department
      List<String> employees = new ArrayList<>();
      
      // Process all values for this key (department ID)
      for (Text val : values) {
        String value = val.toString();
        
        // Department data starts with 'D:'
        if (value.startsWith("D:")) {
          deptName = value.substring(2);
        } 
        // Employee data starts with 'E:'
        else if (value.startsWith("E:")) {
          employees.add(value.substring(2));
        }
      }
      
      // Join department with each employee
      if (!deptName.isEmpty()) {
        for (String employee : employees) {
          String[] parts = employee.split(",");
          String empId = parts[0];
          String empName = parts[1];
          
          outValue.set(empId + "," + empName + "," + deptName);
          context.write(key, outValue);
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "join example");
    job.setJarByClass(JoinExample.class);
    job.setMapperClass(JoinMapper.class);
    job.setReducerClass(JoinReducer.class);
    
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}