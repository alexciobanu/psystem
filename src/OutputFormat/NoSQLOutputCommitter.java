package OutputFormat;

import java.io.IOException;
import java.util.ArrayList;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;

import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;


public class NoSQLOutputCommitter extends OutputCommitter
{
	String addedKeysID;
	
	public NoSQLOutputCommitter(String id)
	{
		addedKeysID = id;	
		NoSQLOutputFormat.addedKeys.put(id, new ArrayList<Key>() );
	}
	
	@Override
	public void abortTask(TaskAttemptContext arg0) throws IOException 
	{
		ArrayList<Key> keysToRemove = NoSQLOutputFormat.addedKeys.get(addedKeysID);
		String storeName = arg0.getConfiguration().get("NoSQLDB.output.Store");
		String hosts = arg0.getConfiguration().get("NoSQLDB.output.Hosts");
		KVStoreConfig config = new KVStoreConfig(storeName, hosts);
        KVStore store = KVStoreFactory.getStore(config);
        for( Key aKey : keysToRemove  )
        {
        	store.delete(aKey);
        }
        NoSQLOutputFormat.addedKeys.remove(addedKeysID);
	}

	@Override
	public void commitTask(TaskAttemptContext arg0) throws IOException 
	{
		NoSQLOutputFormat.addedKeys.remove(addedKeysID);
	}

	@Override
	public boolean needsTaskCommit(TaskAttemptContext arg0) throws IOException 
	{
		return false;
	}

	@Override
	public void setupJob(JobContext arg0) throws IOException 
	{
		// nothing to do 
	}

	@Override
	public void setupTask(TaskAttemptContext arg0) throws IOException 
	{
		// nothing to do
	}

}
