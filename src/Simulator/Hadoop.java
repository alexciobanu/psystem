package Simulator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

import org.apache.avro.generic.IndexedRecord;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import InputFormat.NoSQLInputFormat;

import oracle.kv.Key;

public class Hadoop extends Configured implements Tool 
{
    public static class Map extends Mapper<Key, IndexedRecord, Text, Text> 
    {
        public void map(Key keyArg, IndexedRecord valueArg, Context context) throws IOException, InterruptedException 
        {
        	String level = (keyArg.getMajorPath()).get(0);
    	    String membrane = (keyArg.getMajorPath()).get(1);  
    	    String uuid = (keyArg.getMinorPath()).get(0);
	        ByteBuffer temp = (ByteBuffer) valueArg.get(0);      

		    ByteArrayInputStream bi2 = new ByteArrayInputStream(temp.array());
	        ObjectInputStream in2 = new ObjectInputStream(bi2);
            long[] currentMultiset = null;
			try 
			{
				currentMultiset = (long[]) in2.readObject();
			} 
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
			
			int buff = Integer.parseInt( level.substring(5, level.length()) );
			buff++;
			String nextLevel= "level"+buff;
			//context.write(new Text(level), new Text(membrane));
			String storeName = context.getConfiguration().get("NoSQLDB.input.Store");
			String hosts = context.getConfiguration().get("NoSQLDB.input.Hosts");
    		NodeCalculator nc = new NodeCalculator(membrane,storeName,hosts);
            nc.getAllCombinations(currentMultiset,nextLevel,uuid);
        }
    }

    public int run(String[] args) throws Exception 
    {
        Job job = new Job(getConf());
        job.setJarByClass(Hadoop.class);
        job.setJobName("Psystem Evolution");

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(Map.class);

        job.setInputFormatClass(NoSQLInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        NoSQLInputFormat.setStoreName(job, args[0]);
        NoSQLInputFormat.setMajorKey(job, args[3]);
        NoSQLInputFormat.setHelperHosts(job, args[1]);
        NoSQLInputFormat.setKeysPerTask(job, 20);
        
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception 
    {
        int ret = ToolRunner.run(new Hadoop(), args);
        System.exit(ret);
    }
}
