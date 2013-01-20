package InputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;


public class NoSQLInputFormat<K,V> extends InputFormat<K, V> 
{
	
	@Override
	public RecordReader<K, V> createRecordReader(InputSplit arg0, TaskAttemptContext arg1) throws IOException, InterruptedException 
	{
		return new NoSQLRecordReader<K,V>();
	}

	@Override
	public List<InputSplit> getSplits(JobContext arg0) throws IOException,InterruptedException 
	{
		String MajorKey = arg0.getConfiguration().get("NoSQLDB.input.Key");
		Key myKey = Key.createKey( MajorKey );
		String storeName = arg0.getConfiguration().get("NoSQLDB.input.Store");
		String hosts = arg0.getConfiguration().get("NoSQLDB.input.Hosts");
		
		KVStoreConfig config = new KVStoreConfig(storeName, hosts);
        KVStore store = KVStoreFactory.getStore(config);
		List<InputSplit> inputs = new ArrayList<InputSplit>();
		Iterator<KeyValueVersion> myItterator = store.storeIterator( Direction.UNORDERED,0,myKey,null,null);
		while (myItterator.hasNext())
		{
			Key key = myItterator.next().getKey();
			InputSplit anInput = new NoSQLSplit( key.getMajorPath().get(1), key.getMinorPath().get(0) );
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
	
}
