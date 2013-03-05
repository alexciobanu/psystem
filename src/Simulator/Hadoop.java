package Simulator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import DerivationTreeGenerator.ApplyAllRules;
import DerivationTreeGenerator.BrutForce;
import DerivationTreeGenerator.ChildrenCalculator;
import InputFormat.NoSQLInputFormat;
import Interfaces.AbstractDatabase;
import Interfaces.MultiMembraneMultiset;
import Interfaces.OracleNoSQLDatabase;

import oracle.kv.Key;

public class Hadoop extends Configured implements Tool 
{
    public static class Map extends Mapper<Key, IndexedRecord, Text, Text> 
    {
        public void map(Key keyArg, IndexedRecord valueArg, Context context) throws IOException, InterruptedException 
        {
        	//String level = (keyArg.getMajorPath()).get(0);
    	    String membrane = (keyArg.getMajorPath()).get(1);  
    	    String uuid = (keyArg.getMinorPath()).get(0);
	        ByteBuffer temp = (ByteBuffer) valueArg.get(0);      

		    ByteArrayInputStream bi2 = new ByteArrayInputStream(temp.array());
	        ObjectInputStream in2 = new ObjectInputStream(bi2);
            int[] currentMultiset = null;
			try 
			{
				currentMultiset = (int[]) in2.readObject();
			} 
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
			
			//int buff = Integer.parseInt( level.substring(5, level.length()) );
			//buff++;
			//String nextLevel= "level"+buff;
			String storeName = context.getConfiguration().get("NoSQLDB.input.Store");
			String hosts = context.getConfiguration().get("NoSQLDB.input.Hosts");
			AbstractDatabase db = new OracleNoSQLDatabase(storeName,hosts);
			
			ChildrenCalculator calc = new BrutForce();
			String[] membranesArray = {membrane};
			List<int[]> possiblilities = calc.findAllChildren(currentMultiset, membranesArray, db);
			ArrayList<MultiMembraneMultiset> configurations = ApplyAllRules.getMulisets(possiblilities,membrane , db);
			for(MultiMembraneMultiset aConfig :  configurations)
			{
				int[] results = aConfig.getMulisetForMembrane(membrane);
				context.write(new Text(uuid), new Text(Arrays.toString( results )));
			}
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
        NoSQLInputFormat.setKeysPerTask(job, 300);
        
        int milliSeconds = 1000*60*60*3; 
        job.getConfiguration().setLong("mapred.task.timeout", milliSeconds);
        job.getConfiguration().setLong("mapred.skip.map.max.skip.records", Long.MAX_VALUE);
        job.getConfiguration().setLong("mapred.map.max.attempts", 10);
        job.getConfiguration().setLong("mapred.skip.attempts.to.start.skipping", 3);
        job.getConfiguration().setLong("mapred.map.failures.maxpercent", 50);
        
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
