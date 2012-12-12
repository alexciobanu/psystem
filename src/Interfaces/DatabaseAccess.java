package Interfaces;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.avro.AvroCatalog;
import oracle.kv.avro.GenericAvroBinding;

public class DatabaseAccess 
{
	private static DatabaseAccess instance = null;
	KVStore store;
	Schema schema;
	GenericAvroBinding binding;
	
	public static DatabaseAccess getInstance() 
	{
	      if(instance == null) 
	      {
	         instance = new DatabaseAccess();
	      }
	      return instance;
	   }
	
	public DatabaseAccess()
	{
		initDatabase();
	}
	
	void initDatabase()
	{
		KVStoreConfig config = new KVStoreConfig("kvstore", "localhost:5000");
        store = KVStoreFactory.getStore(config);
        AvroCatalog catalog = store.getAvroCatalog(); 
        String s =" {\"type\" : \"record\",\"name\" : \"dataRecord\",\"fields\" : [ {\"name\" : \"data\", \"type\": \"bytes\"}]}";
        Schema.Parser parser = new Schema.Parser();
        parser.parse(s);
        schema = parser.getTypes().get("dataRecord"); 
        binding = catalog.getGenericBinding(schema);
	}
	
	public int store(Object Majorkey, Object MinorKey, Object value)
	{
		Key myKey = makeKey(Majorkey, MinorKey);    
        GenericData.Record record = new GenericData.Record(schema);
        ByteBuffer buffer = ByteBuffer.wrap((byte []) value);
        record.put("data", buffer);
        Value KVvalue = binding.toValue(record);
        store.put(myKey, KVvalue );  
		return 0;
	}
	
	public byte[] retrieve(Object Majorkey, Object MinorKey)
	{
		Key myKey = makeKey(Majorkey, MinorKey);
        ValueVersion myInput = store.get(myKey);
        GenericRecord inputRecord = new GenericData.Record(schema);
        inputRecord  = binding.toObject(myInput.getValue());
        ByteBuffer temp = (ByteBuffer) inputRecord.get(0);
		return temp.array();
	}
	public List<byte[]> multiRetrieve(Object Majorkey, Object MinorKey)
	{
		Key myKey = makeKey(Majorkey, MinorKey);
		SortedMap<Key, ValueVersion> entries = store.multiGet(myKey, null, null);
		List<byte []> rawValues = new ArrayList<byte[]>();
		for(Entry<Key, ValueVersion> anEntry: entries.entrySet())
		{
	        GenericRecord inputRecord = new GenericData.Record(schema);
	        inputRecord  = binding.toObject(anEntry.getValue().getValue());
	        ByteBuffer temp = (ByteBuffer) inputRecord.get(0);
			rawValues.add( temp.array() );
		}
		return rawValues;
	}
	
	public List<byte[]> multiRetrieveParcialMajor(Object Majorkey)
	{
		Key myKey = makeKey(Majorkey, null);
		List<byte []> rawValues = new ArrayList<byte[]>();
		Iterator<KeyValueVersion> myItterator = store.storeIterator( Direction.UNORDERED,0,myKey,null,null);
		while (myItterator.hasNext())
		{
	        GenericRecord inputRecord = new GenericData.Record(schema);
	        inputRecord  = binding.toObject(myItterator.next().getValue());
	        ByteBuffer temp = (ByteBuffer) inputRecord.get(0);
			rawValues.add( temp.array() );
		}
		return rawValues;
	}
	
	@SuppressWarnings("unchecked")
	private Key makeKey(Object Majorkey, Object MinorKey)
	{
		Key myKey = null;
		if (MinorKey==null)
		{
			if (Majorkey instanceof String)
			{
				myKey = Key.createKey((String) Majorkey);
			}
			else if (Majorkey instanceof List<?>)
			{
				myKey = Key.createKey( (List<String>) Majorkey);
			}
			else
			{
				myKey = Key.createKey(Majorkey.toString());
			}
		}
		else
		{
			if ((Majorkey instanceof String) && (MinorKey instanceof String)  )
			{
				myKey = Key.createKey((String) Majorkey, (String) MinorKey);
			}
			else if ((Majorkey instanceof List<?>) && (MinorKey instanceof String)  )
			{
				myKey = Key.createKey( (List<String>) Majorkey, (String) MinorKey);
			}
			else if ((Majorkey instanceof String) && (MinorKey instanceof List<?>)  )
			{
				myKey = Key.createKey( (String) Majorkey, (List<String>) MinorKey);
			}
			else if ((Majorkey instanceof List<?>) && (MinorKey instanceof List<?>)  )
			{
				myKey = Key.createKey( (List<String>) Majorkey, (List<String>) MinorKey);
			}
			else if (Majorkey instanceof List<?>) 
			{
				myKey = Key.createKey( (List<String>) Majorkey, MinorKey.toString());
			}
			else if (Majorkey instanceof String) 
			{
				myKey = Key.createKey( (String) Majorkey, MinorKey.toString());
			}
			else if (MinorKey instanceof List<?>) 
			{
				myKey = Key.createKey( Majorkey.toString(), (List<String>) MinorKey );
			}
			else if ((MinorKey instanceof String) )
			{
				myKey = Key.createKey( Majorkey.toString(), (String) MinorKey );
			}
			else
			{
				myKey = Key.createKey( Majorkey.toString(), MinorKey.toString() );
			}	
		}
		return myKey;
	}

}
