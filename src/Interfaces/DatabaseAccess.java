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

import oracle.kv.Consistency;
import oracle.kv.Direction;
import oracle.kv.Durability;
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
	Schema nodeSchema;
	GenericAvroBinding binding;
	GenericAvroBinding nodeBinding;
	
	public static DatabaseAccess getInstance(String storeName, String hosts) 
	{
		if(instance == null) 
	    {
			instance = new DatabaseAccess(storeName,hosts);
	    }
		return instance;
	}
	
	public DatabaseAccess(String storeName, String hosts)
	{
		initDatabase(storeName,hosts);
	}
	
	void initDatabase(String storeName, String hosts)
	{
		KVStoreConfig config = new KVStoreConfig(storeName, hosts);
        store = KVStoreFactory.getStore(config);
        AvroCatalog catalog = store.getAvroCatalog(); 
        
        String s2 = " { \"type\" : \"record\",\"name\" : \"nodeRecord\", \"fields\" : ["
          	     + "{ \"name\" :\"data\", \"type\" :\"bytes\", \"default\" :\"null\" },"
                   + "{ \"name\" :\"rules\", \"type\" :\"bytes\", \"default\" :\"null\" },"
                   + "{ \"name\" :\"parent\", \"type\" :\"string\", \"default\" :\"null\" },"
   	               + "{ \"name\" :\"duplicate\", \"type\" :\"string\", \"default\" :\"\" }"
          		 + "]}";
        Schema.Parser parser2 = new Schema.Parser();
        parser2.parse(s2);
        nodeSchema = parser2.getTypes().get("nodeRecord"); 
        nodeBinding = catalog.getGenericBinding(nodeSchema);
        
        String s = " {\"type\" : \"record\",\"name\" : \"dataRecord\", \"fields\" : ["
        	     + "{ \"name\" :\"data\", \"type\" :\"bytes\", \"default\" :\"null\" }]}";      
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
        store.put(myKey, KVvalue);  
		return 0;
	}
	
	public int storeNode(Object Majorkey, Object MinorKey, Object data, Object rules, String parent)
	{
		//System.out.println("Storing Node" + Majorkey + MinorKey);
		Key myKey = makeKey(Majorkey, MinorKey);    
        GenericData.Record record = new GenericData.Record(nodeSchema);
        ByteBuffer buffer = ByteBuffer.wrap((byte []) data);
        ByteBuffer buffer2 = ByteBuffer.wrap((byte []) rules);
        record.put("data", buffer);
        record.put("rules",buffer2);
        record.put("duplicate", "");
        record.put("parent", parent);
        Value KVvalue = nodeBinding.toValue(record);
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
	
	public byte[] retrieveWithNull(Object Majorkey, Object MinorKey)
	{
		Key myKey = makeKey(Majorkey, MinorKey);
        ValueVersion myInput = store.get(myKey);
        if (myInput==null)
        	return null;
        GenericRecord inputRecord = new GenericData.Record(schema);
        inputRecord  = binding.toObject(myInput.getValue());
        ByteBuffer temp = (ByteBuffer) inputRecord.get(0);
		return temp.array();
	}
	
	public boolean delete(Object Majorkey, Object MinorKey)
	{
		Key myKey = makeKey(Majorkey, MinorKey);
        return store.delete(myKey, null, Durability.COMMIT_SYNC, 0, null);
	}
	
	public NodeData retrieveNode(Object Majorkey, Object MinorKey)
	{
		Key myKey = makeKey(Majorkey, MinorKey);
        ValueVersion myInput = store.get(myKey);
        GenericRecord inputRecord = new GenericData.Record(nodeSchema);
        inputRecord  = nodeBinding.toObject(myInput.getValue());
        NodeData aNode = new NodeData();
        aNode.data = ((ByteBuffer) inputRecord.get(0)).array();
        aNode.rules = ((ByteBuffer) inputRecord.get(1)).array();
        aNode.parent = inputRecord.get(2).toString();
		return aNode;
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
	
	public List<NodeData> multiRetrieveNodeSchema(Object Majorkey)
	{
		Key myKey = makeKey(Majorkey, null);
		List<NodeData> rawValues = new ArrayList<NodeData>();
		Iterator<KeyValueVersion> myItterator = store.storeIterator( Direction.UNORDERED,0,myKey,null,null);
		while (myItterator.hasNext())
		{
	        GenericRecord inputRecord = new GenericData.Record(nodeSchema);
	        inputRecord  = nodeBinding.toObject(myItterator.next().getValue());
	        NodeData aNode = new NodeData();
	        aNode.data = ((ByteBuffer) inputRecord.get(0)).array();
	        aNode.rules = ((ByteBuffer) inputRecord.get(1)).array();
	        aNode.parent = inputRecord.get(2).toString();
			rawValues.add( aNode );
		}
		return rawValues;
	}
	
	public int countObjects(Object Majorkey)
	{
		Key myKey = makeKey(Majorkey, null);
		int numElements=0;
		Iterator<KeyValueVersion> myItterator = store.storeIterator( Direction.UNORDERED,0,myKey,null,null);
		while (myItterator.hasNext())
		{
			numElements++;
			myItterator.next();
		}
		return numElements;
	}
	
	public boolean multiDelete(Object Majorkey)
	{
		Key myKey = makeKey(Majorkey, null);
		store.multiDelete(myKey, null, null);
		return true;
	}
	
	public void printAllKeys()
	{
		Iterator<KeyValueVersion> myItterator = store.storeIterator( Direction.UNORDERED,0,null,null,null,Consistency.ABSOLUTE,0,null);
		while (myItterator.hasNext())
		{
			System.out.println(myItterator.next().getKey());
		}		
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
