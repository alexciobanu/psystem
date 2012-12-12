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

import oracle.kv.Key;
import oracle.kv.hadoop.KVAvroInputFormat;
import oracle.kv.hadoop.KVInputFormat;
import oracle.kv.hadoop.KVInputFormatBase;

public class Hadoop extends Configured implements Tool 
{
    public static class Map extends Mapper<Key, IndexedRecord, Text, Text> 
    {
        public void map(Key keyArg, IndexedRecord valueArg, Context context) throws IOException, InterruptedException 
        {
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
    		NodeCalculator nc = new NodeCalculator(membrane);
            nc.getAllCombinations(currentMultiset,"level1",uuid);
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

        job.setInputFormatClass(KVAvroInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        KVInputFormatBase.setFormatterClassName("MyAvroFormatter");

        KVInputFormat.setKVStoreName(args[0]);
        KVInputFormat.setParentKey(Key.createKey("level0"));
        KVInputFormat.setKVHelperHosts(new String[] { args[1] });
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
