package InputFormat;

import java.io.IOException;
import java.util.ArrayList;

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
	ArrayList<Key> keys;
	int currentIndex;
	int NumberOfKeys;
	KVStore store;
	GenericAvroBinding binding;
	
	@Override
	public void initialize(InputSplit arg0, TaskAttemptContext arg1)
	{
		System.out.println("");
		String storeName = arg1.getConfiguration().get("NoSQLDB.input.Store");
		String hosts = arg1.getConfiguration().get("NoSQLDB.input.Hosts");
		
		KVStoreConfig config = new KVStoreConfig(storeName, hosts);
        store = KVStoreFactory.getStore(config);
        NoSQLSplit split = (NoSQLSplit) arg0; 
        
        keys = split.getKeys();
        NumberOfKeys = keys.size();
        currentIndex=-1;
                
        AvroCatalog catalog = store.getAvroCatalog(); 
        String s = " { \"type\" : \"record\",\"name\" : \"nodeRecord\", \"fields\" : ["
         	     + "{ \"name\" :\"multiset\", \"type\" :\"bytes\", \"default\" :\"null\" },"
                  + "{ \"name\" :\"rules\", \"type\" :\"bytes\", \"default\" :\"null\" },"
                  + "{ \"name\" :\"parent\", \"type\" :\"string\", \"default\" :\"null\" },"
  	               + "{ \"name\" :\"duplicate\", \"type\" :\"string\", \"default\" :\"\" }"
         		 + "]}";
        Schema.Parser parser = new Schema.Parser();
        parser.parse(s);
        Schema schema = parser.getTypes().get("nodeRecord"); 
        binding = catalog.getGenericBinding(schema);
	}
	
	@Override
	public void close() throws IOException 
	{
        store.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public K getCurrentKey() 
	{
		//System.out.println("GETTIG KEY:" + keys.get(currentIndex));
		return (K) keys.get(currentIndex);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V getCurrentValue() 
	{
		ValueVersion inputData = store.get( keys.get(currentIndex) );
		return (V) binding.toObject( inputData.getValue() );
	}

	@Override
	public float getProgress() 
	{
		return currentIndex/NumberOfKeys;
	}

	@Override
	public boolean nextKeyValue() 
	{
		currentIndex++;
		if (currentIndex<NumberOfKeys)
			return true;
		else
			return false;
	}
}
