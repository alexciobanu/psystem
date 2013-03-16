package InputFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;


public class NoSQLInputFormat<K,V> extends InputFormat<K, V> 
{
	@Override
	public RecordReader<K, V> createRecordReader(InputSplit arg0, TaskAttemptContext arg1) 
	{
		return new NoSQLRecordReader<K,V>();
	}

	@Override
	public List<InputSplit> getSplits(JobContext arg0) 
	{
		String MajorKey = arg0.getConfiguration().get("NoSQLDB.input.Key");
		Key myKey = Key.createKey( MajorKey );
		String storeName = arg0.getConfiguration().get("NoSQLDB.input.Store");
		String hosts = arg0.getConfiguration().get("NoSQLDB.input.Hosts");
		int numberOfKeysPerSplit = arg0.getConfiguration().getInt("NoSQLDB.input.KeysPerSplit", 1);
		KVStoreConfig config = new KVStoreConfig(storeName, hosts);
        KVStore store = KVStoreFactory.getStore(config);
		List<InputSplit> inputs = new ArrayList<InputSplit>();
		Iterator<Key> myItterator = store.storeKeysIterator( Direction.UNORDERED,0,myKey,null,null);
		ArrayList<Key> keysOfASplit = new ArrayList<Key>();
		int i=0;
		Key aKey;
		//int numSplits=0;
		while (myItterator.hasNext())
		{
			if (i<numberOfKeysPerSplit)
			{
				aKey = myItterator.next();
				keysOfASplit.add(aKey);
				i++;
			}
			else
			{	
				/*numSplits++;
				if (numSplits % 1000 == 0)
				{
					System.out.println("We have: " + inputs.size()+1 + " splits");
				}*/
				InputSplit anInput = new NoSQLSplit( keysOfASplit );
				inputs.add(anInput);
				keysOfASplit = new ArrayList<Key>();
				i=0;
			}
		}
		if (i!=0)
		{
			InputSplit anInput = new NoSQLSplit( keysOfASplit );
			inputs.add(anInput);
		}
		return inputs;
	}
	
	public static void setMajorKey(Job job, String majorKey)
	{
		job.getConfiguration().set("NoSQLDB.input.Key", majorKey.toString());
	}
	
	public static void setStoreName(Job job, String store)
	{
		job.getConfiguration().set("NoSQLDB.input.Store", store);
	}
	
	public static void setHelperHosts(Job job,String hosts )
	{
		job.getConfiguration().set("NoSQLDB.input.Hosts", hosts);
	}
	
	public static void setKeysPerTask(Job job, int numberOfKeys)
	{
		job.getConfiguration().setInt("NoSQLDB.input.KeysPerSplit", numberOfKeys);
	}
	
}
