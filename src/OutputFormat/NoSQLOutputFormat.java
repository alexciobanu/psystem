package OutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskID;

public class NoSQLOutputFormat<K,V> extends OutputFormat<K,V>
{
	static HashMap<String,ArrayList<Key>> addedKeys;

	public NoSQLOutputFormat()
	{
		super();
		addedKeys = new HashMap<String,ArrayList<Key>>();
	}
	
	@Override
	public void checkOutputSpecs(JobContext arg0) throws IOException, InterruptedException 
	{
		String storeName = arg0.getConfiguration().get("NoSQLDB.output.Store");
		String hosts = arg0.getConfiguration().get("NoSQLDB.output.Hosts");
		if ((storeName == null) || (hosts == null))
		{
			System.out.println("NoSQLDB.input.Store or NoSQLDB.input.Hosts not set");
			throw new IOException();
		}
		KVStoreConfig config = new KVStoreConfig(storeName, hosts);
		try
		{
        	@SuppressWarnings("unused")
			KVStore store = KVStoreFactory.getStore(config);
		}
		catch (Exception e) 
		{
			System.out.println("NoSQLDB.input.Store or NoSQLDB.input.Hosts not set correctly or the databse is not create");
			throw new IOException();
		}
	}

	@Override
	public OutputCommitter getOutputCommitter(TaskAttemptContext arg0) throws IOException, InterruptedException 
	{
		OutputCommitter committer = new NoSQLOutputCommitter(getuniqueId(arg0));
		return committer;
	}

	@Override
	public org.apache.hadoop.mapreduce.RecordWriter<K, V> getRecordWriter(TaskAttemptContext arg0) throws IOException, InterruptedException 
	{
		String storeName = arg0.getConfiguration().get("NoSQLDB.output.Store");
		String hosts = arg0.getConfiguration().get("NoSQLDB.output.Hosts");
		RecordWriter<K, V> writer = new NoSQLRecordWriter<K, V>(getuniqueId(arg0),storeName,hosts);
		return writer;
	}
	
	public String getuniqueId(TaskAttemptContext context)
	{
		 TaskID taskId = context.getTaskAttemptID().getTaskID();
		 int partition = taskId.getId();
		 taskId.toString();
		 StringBuilder result = new StringBuilder();
		 result.append(taskId.toString());
		 result.append('-');
		 result.append(taskId.isMap() ? 'm' : 'r');
		 result.append('-');
		 result.append( Integer.toString(partition) );
		 return result.toString();	
	}
	
	public static void setStoreName(Job job, String store)
	{
		job.getConfiguration().set("NoSQLDB.output.Store", store);
	}
	
	public static void setHelperHosts(Job job,String hosts )
	{
		job.getConfiguration().set("NoSQLDB.output.Hosts", hosts);
	}
}
