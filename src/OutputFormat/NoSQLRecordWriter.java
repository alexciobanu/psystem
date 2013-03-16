package OutputFormat;

import java.io.IOException;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class NoSQLRecordWriter<K,V> extends RecordWriter<K, V> 
{
	
	KVStore store;
	String attemptID;

	public NoSQLRecordWriter(String id, String storeName, String hosts)
	{
		KVStoreConfig config = new KVStoreConfig(storeName, hosts);
        store = KVStoreFactory.getStore(config);
        attemptID = id;
	}

	@Override
	public void write(K arg0, V arg1) throws IOException 
	{
		Key key;
		Value value;
		if  ((arg0 instanceof KeyWritable) && (arg1 instanceof Value))
		{
			key = ((KeyWritable) arg0).getKey();
			value=(Value) arg1;
		}
		else
		{
			System.out.println("Non Compatible Key type or Value type");
			throw new IOException();
		}
		store.put(key, value);
		(NoSQLOutputFormat.addedKeys.get(attemptID)).add(key);
	}

	@Override
	public void close(TaskAttemptContext arg0) 
	{
		store.close();
	}

}
