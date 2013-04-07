package Simulator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.avro.generic.IndexedRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import DerivationTreeGenerator.ApplyAllRules;
import DerivationTreeGenerator.BrutForce;
import DerivationTreeGenerator.ChildrenCalculator;
import DerivationTreeGenerator.EquationSolver;
import InputFormat.NoSQLInputFormat;
import Interfaces.AbstractDatabase;
import Interfaces.MembranePosibitities;
import Interfaces.MultiMembraneMultiset;
import Interfaces.NodeData;
import Interfaces.OracleNoSQLDatabase;
import OutputFormat.KeyWritable;
import OutputFormat.NoSQLOutputFormat;

import oracle.kv.Key;
import oracle.kv.Value;

public class DerivationTreeGenerator 
{
    public static class Map extends Mapper<Key, IndexedRecord, Text, MembranePosibitities> 
    {
        public void map(Key keyArg, IndexedRecord valueArg, Context context) throws IOException, InterruptedException 
        {
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
			
			String storeName = context.getConfiguration().get("NoSQLDB.input.Store");
			String hosts = context.getConfiguration().get("NoSQLDB.input.Hosts");
			AbstractDatabase db = new OracleNoSQLDatabase(storeName,hosts);
			ChildrenCalculator calc;
			if(db.CheckForSolutionMatrix(membrane))
			{
				calc = new BrutForce();
			}
			else
			{
				calc = new EquationSolver();
			}
			List<int[]> possiblilities = calc.findAllChildren(currentMultiset, membrane, db);
			IntWritable[][]  data = new IntWritable[possiblilities.size()][possiblilities.get(0).length];
			for(int i=0;i<possiblilities.size();i++)
			{
				int[] currentList= possiblilities.get(i);
				for(int j=0;j<currentList.length;j++)
				{
					int aValue = possiblilities.get(i)[j];
					data[i][j] = new IntWritable( aValue );
				}
			}
			context.write(new Text(uuid), new MembranePosibitities(data,membrane)   );
			
        }
    }
    
    public static class Reduce extends Reducer<Text , MembranePosibitities, KeyWritable, Value> 
    {
    	public void reduce(Text key, Iterable<MembranePosibitities> values, Context context) throws IOException, InterruptedException 
    	{
    		String currentlevel = context.getConfiguration().get("NoSQLDB.input.Key");
			int nextLevel = Integer.parseInt( currentlevel ) + 1;
			String storeName = context.getConfiguration().get("NoSQLDB.input.Store");
			String hosts = context.getConfiguration().get("NoSQLDB.input.Hosts");
			AbstractDatabase db = new OracleNoSQLDatabase(storeName,hosts);
			
			ArrayList<IntWritable [][]> valuesArray = new ArrayList<IntWritable [][]>();
			ArrayList<String> membranes= new ArrayList<String>();

			for (MembranePosibitities anRarray : values)
			{
				valuesArray.add( (IntWritable[][]) anRarray.getPossibilities() );
				membranes.add(anRarray.getMembrane());
			}
			
			int[] numberOfPossibilities = new int[valuesArray.size()];
			int numberOfMembranes =  numberOfPossibilities.length;
			
			int i=0;
			for (IntWritable[][] aValue : valuesArray)
			{
				numberOfPossibilities[i] = aValue.length;
				i++;
			}
			
			int counter[]= new int[numberOfMembranes]; //this is will a counter of the current combination being tried
			boolean breakCondition=false;
			ArrayList<String> children = new ArrayList<String>();
			while(!breakCondition)
			{	
				int[] aCombination = new int[numberOfMembranes];
				for(i=0;i<numberOfMembranes;i++)
				{
					aCombination[i] = counter[i];
				}
				counter[0]+=1;
				for(i=0;i<numberOfMembranes;i++)
				{
					if (counter[i]>=numberOfPossibilities[i])
					{
						if (i==(numberOfMembranes-1))
						{
							breakCondition=true;
							break;
						}
						else
						{
							counter[i+1]+=1;
							counter[i]=0;
						}
					}
				}
				MultiMembraneMultiset configuration = new MultiMembraneMultiset();
				HashMap<String,IntWritable []> rulesApplied = new HashMap<String,IntWritable []>(); 
				for(i=0;i<aCombination.length;i++)
				{
					ApplyAllRules.addMulisetsToConfiguration(configuration, (valuesArray.get(i))[aCombination[i]] ,membranes.get(i) , db);
					rulesApplied.put(membranes.get(i), (valuesArray.get(i))[aCombination[i]]);
				}
				String uuid = UUID.randomUUID().toString();
				children.add(uuid);
				//Save rules applied
				db.StoreAppliedRules(uuid, rulesApplied);
				for(String aMembrane: configuration.getMembranes())
				{
					int[] aconfig = configuration.getMulisetForMembrane(aMembrane);
					NodeData aNode = new NodeData();
					aNode.parent=key.toString();
					aNode.multiset=aconfig;
					List<String> majorComponents = Arrays.asList(Integer.toString(nextLevel),aMembrane);
					Key myKey = Key.createKey(majorComponents, uuid); 
					context.write( new KeyWritable(myKey), db.createNodeValue(aNode) );	
				}
			}
			db.StoreChildren(key.toString(), children);
    	}
   	
    }


    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException 
    {
    	Configuration conf = new Configuration();
    	Job job = new Job(conf, "Psystem Evolution");
        job.setJarByClass(DerivationTreeGenerator.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);
        
        //Input Definition
        job.setInputFormatClass(NoSQLInputFormat.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(MembranePosibitities.class);
        NoSQLInputFormat.setStoreName(job, args[0]);
        NoSQLInputFormat.setMajorKey(job, args[2]);
        NoSQLInputFormat.setHelperHosts(job, args[1]);
        NoSQLInputFormat.setKeysPerTask(job, 300);
        
        //Output Definition
        job.setOutputFormatClass(NoSQLOutputFormat.class);  
        job.setOutputKeyClass(KeyWritable.class);
        job.setOutputValueClass(Value.class);
        NoSQLOutputFormat.setHelperHosts(job, args[1]);
        NoSQLOutputFormat.setStoreName(job, args[0]);
        
        //Cluster Configuration
        int milliSeconds = 1000*60*60*3; 
        job.getConfiguration().setLong("mapred.task.timeout", milliSeconds);
        /*job.getConfiguration().setLong("mapred.skip.map.max.skip.records", Long.MAX_VALUE);
        job.getConfiguration().setLong("mapred.map.max.attempts", 10);
        job.getConfiguration().setLong("mapred.skip.attempts.to.start.skipping", 3);
        job.getConfiguration().setLong("mapred.map.failures.maxpercent", 50);*/
        
        System.exit( job.waitForCompletion(true) ? 0 : 1);
    }
}
