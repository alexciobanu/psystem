package CDRCTesting;

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

import Interfaces.DatabaseAccess;
import Interfaces.NodeData;

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
        	String level = (keyArg.getMajorPath()).get(0);
    	    String membrane = (keyArg.getMajorPath()).get(1);  
    	    String uuid = (keyArg.getMinorPath()).get(0);
	        ByteBuffer temp = (ByteBuffer) valueArg.get(1);
	        
			int buff = Integer.parseInt( level.substring(5, level.length()) );
			buff++;
			String nextLevel= "level"+buff;

		    ByteArrayInputStream bi2 = new ByteArrayInputStream(temp.array());
	        ObjectInputStream in2 = new ObjectInputStream(bi2);
            long[] currentRule = null;
            ArrayList<long[]> allChildren =null;
			try 
			{
				currentRule = (long[]) in2.readObject();
				allChildren = getChildren(uuid,nextLevel,membrane);
			} 
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
			
			
	        DatabaseAccess db = new DatabaseAccess();
	        List<String> majorComponent = Arrays.asList("CDRCrules",membrane);
	        for(long[] aChild: allChildren)
	        {
	        	for(int i=0;i<currentRule.length;i++)
	        	{
	        		if (currentRule[i]>0)
	        		{
	        			for(int j=0;j<aChild.length;j++)
	        			{
	        				if (aChild[j]>0)
	        				{
	        					List<String> cdrcPair = Arrays.asList(Integer.toString(i) ,(Integer.toString(j)));
	        					if( db.retrieveWithNull(majorComponent,cdrcPair) != null )
	        					{
	        						String output = new String();
	        						output = ":";//cdrcPair + ": ";
	        						try 
	        						{
										output += getParentRules(level,membrane,uuid);
									} 
	        						catch (ClassNotFoundException e) 
	        						{
										e.printStackTrace();
									}
	        						output += Arrays.toString(currentRule) + ", " + Arrays.toString(aChild);
	        						db.delete(majorComponent,cdrcPair);
	        						context.write(new Text(cdrcPair.toString()), new Text(output));
	        						//System.out.println(output);
	        					}
	        					
	        				}
	        			}
	        		}
	        	}
	        }
        }
        
        public String getParentRules(String level, String membrane, String uuid) throws IOException, ClassNotFoundException
    	{	
    		int levelNumber = Integer.parseInt( level.substring(5, level.length()) );
    		String output = new String();
    		while(levelNumber>1)
    		{	
    			String curentLevel= "level"+levelNumber;
    			List<String> majorComponent = Arrays.asList(curentLevel, membrane);
    			DatabaseAccess db = new DatabaseAccess();
    			NodeData temp = db.retrieveNode(majorComponent,uuid);
    			String parent = temp.parent;
    		
    			levelNumber--;
    			String previousLevel= "level"+levelNumber;
    			
    			majorComponent = Arrays.asList(previousLevel, membrane);
    			NodeData parentNode = db.retrieveNode(majorComponent, parent);
    			byte[] rules = parentNode.rules;
    			ByteArrayInputStream bi2 = new ByteArrayInputStream(rules);
    	        ObjectInputStream in2 = new ObjectInputStream(bi2);
    			long[] rulesArray = (long[]) in2.readObject();
    			output+= Arrays.toString( rulesArray) + ", ";
    		}
    		return output;
    		
    	}
    	
    	public ArrayList<long[]> getChildren(String uuid,String level, String membrane) throws IOException, ClassNotFoundException
    	{
    		ArrayList<long[]> allChildren = new ArrayList<long[]>();
    		DatabaseAccess db = new DatabaseAccess();
    		byte[] temp = db.retrieve(uuid,null);
    		ByteArrayInputStream bi2 = new ByteArrayInputStream(temp);
            ObjectInputStream in2 = new ObjectInputStream(bi2);
            @SuppressWarnings("unchecked")
    		ArrayList<String> children = (ArrayList<String>) in2.readObject();
            for(int i=0;i<children.size();i++)
            {
            	long[] childRules = getChildRules(children.get(i),level,membrane);
            	allChildren.add(childRules);
            }
            return allChildren;
    	}
    	
    	public long[] getChildRules(String uuid,String level, String membrane) throws IOException, ClassNotFoundException
    	{
    		List<String> majorComponents = Arrays.asList(level,membrane);
    		DatabaseAccess db = new DatabaseAccess();
    		NodeData temp = db.retrieveNode(majorComponents,uuid);
    		byte[] rules = temp.rules;
    		ByteArrayInputStream bi2 = new ByteArrayInputStream(rules);
            ObjectInputStream in2 = new ObjectInputStream(bi2);
    		long[] rulesArray = (long[]) in2.readObject();
    		return rulesArray;
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
        KVInputFormatBase.setFormatterClassName("Simulator.MyAvroFormatter");

        KVInputFormat.setKVStoreName(args[0]);
        KVInputFormat.setKVHelperHosts(new String[] { args[1] });
        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        KVInputFormat.setParentKey(Key.createKey( args[3] ));

        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception 
    {
        int ret = ToolRunner.run(new Hadoop(), args);
        System.exit(ret);
    }
}
	

