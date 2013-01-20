package InputFormat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.ValueVersion;
import oracle.kv.avro.AvroCatalog;
import oracle.kv.avro.GenericAvroBinding;

import org.apache.avro.Schema;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class NoSQLRecordReader<K, V> extends RecordReader<K, V> 
{
	K CurrentKey;
	V CurentValue;
	KVStore store;
	boolean read;
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(InputSplit arg0, TaskAttemptContext arg1) throws IOException, InterruptedException 
	{
		String storeName = arg1.getConfiguration().get("NoSQLDB.input.Store");
		String hosts = arg1.getConfiguration().get("NoSQLDB.input.Hosts");
		String majorKey = arg1.getConfiguration().get("NoSQLDB.input.Key");
		
		KVStoreConfig config = new KVStoreConfig(storeName, hosts);
        store = KVStoreFactory.getStore(config);
        NoSQLSplit split = (NoSQLSplit) arg0; 
		List<String> majorPath = Arrays.asList(majorKey,split.membrane);
		Key currentKey = Key.createKey(majorPath,split.uuid);
        ValueVersion inputData = store.get( currentKey );
        System.out.println(split.membrane + " : " +  split.uuid);
        System.out.println(currentKey);
                
        AvroCatalog catalog = store.getAvroCatalog(); 
        String s = " { \"type\" : \"record\",\"name\" : \"nodeRecord\", \"fields\" : ["
         	     + "{ \"name\" :\"data\", \"type\" :\"bytes\", \"default\" :\"null\" },"
                  + "{ \"name\" :\"rules\", \"type\" :\"bytes\", \"default\" :\"null\" },"
                  + "{ \"name\" :\"parent\", \"type\" :\"string\", \"default\" :\"null\" },"
  	               + "{ \"name\" :\"duplicate\", \"type\" :\"string\", \"default\" :\"\" }"
         		 + "]}";
        Schema.Parser parser = new Schema.Parser();
        parser.parse(s);
        Schema schema = parser.getTypes().get("nodeRecord"); 
        GenericAvroBinding binding = catalog.getGenericBinding(schema);

        CurentValue = (V) binding.toObject(inputData.getValue());
        CurrentKey = (K) currentKey;
        read=false;
	}
	
	@Override
	public void close() throws IOException 
	{
        store.close();
	}

	@Override
	public K getCurrentKey() throws IOException, InterruptedException 
	{
		return CurrentKey;
	}

	@Override
	public V getCurrentValue() throws IOException, InterruptedException 
	{
		read=true;
		return CurentValue;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException 
	{
		return 1;
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException 
	{
		if (read)
			return false;
		else
			return true;
	}


}
