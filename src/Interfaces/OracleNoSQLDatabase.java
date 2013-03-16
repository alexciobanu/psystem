package Interfaces;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import Interfaces.MultiMembraneMultiset;

public class OracleNoSQLDatabase implements AbstractDatabase {

	private static OracleNoSQLDatabase instance = null;
	KVStore store;
	Schema schema;
	Schema nodeSchema;
	GenericAvroBinding binding;
	GenericAvroBinding nodeBinding;
	
	public static OracleNoSQLDatabase getInstance(String storeName, String hosts) 
	{
		if(instance == null) 
	    {
			instance = new OracleNoSQLDatabase(storeName,hosts);
	    }
		return instance;
	}
	
	public OracleNoSQLDatabase(String storeName, String hosts)
	{
		String argv[]= new String[2];
		argv[0]=storeName;
		argv[1]=hosts;
		initDatabase(argv);
	}
	@Override
	public void initDatabase(String[] argv) 
	{
		//System.out.println("we are initializing the database");
		KVStoreConfig config = new KVStoreConfig(argv[0], argv[1]);
		config.setRequestTimeout(120, TimeUnit.SECONDS);
		config.setSocketReadTimeout(120, TimeUnit.SECONDS);
		config.setSocketOpenTimeout(120, TimeUnit.SECONDS);

		
        store = KVStoreFactory.getStore(config);
        AvroCatalog catalog = store.getAvroCatalog(); 
        
        String s2 = " { \"type\" : \"record\",\"name\" : \"nodeRecord\", \"fields\" : ["
          	     + "{ \"name\" :\"multiset\", \"type\" :\"bytes\", \"default\" :\"null\" },"
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

	@Override
	public void storeMembraneElement(String membrane, String element, Object data) 
	{
		ArrayList<String> allPossibilities = new ArrayList<String> (Arrays.asList("left","right","ParentMembrane", "ChildrenMembranes"));
		if (allPossibilities.contains(element))
		{
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream out;
			try 
			{
				out = new ObjectOutputStream(bo);
				out.writeObject(data);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			List<String> majorComponents = Arrays.asList("membrane",membrane);
			Key myKey = Key.createKey(majorComponents ,element);    
	        GenericData.Record record = new GenericData.Record(schema);
	        ByteBuffer buffer = ByteBuffer.wrap((byte []) bo.toByteArray());
	        record.put("data", buffer);
	        Value KVvalue = binding.toValue(record);
	        store.put(myKey, KVvalue);  
		}
		else 
		{
			System.out.println("Not an acceplatble Memebrane element to store");
			System.exit(-1);
		}
	}
	
	@Override
	public Object retriveMembraneElement(String membrane, String element) 
	{
		ArrayList<String> allPossibilities = new ArrayList<String> (Arrays.asList("left","right","ParentMembrane", "ChildrenMembranes"));
		if (allPossibilities.contains(element))
		{
			Object data;
			try 
			{
				List<String> majorComponents = Arrays.asList("membrane",membrane);
				Key myKey = Key.createKey(majorComponents,element); 
		        ValueVersion myInput = store.get(myKey);
		        if (myInput==null)
		        	return null;
		        GenericRecord inputRecord = new GenericData.Record(schema);
		        inputRecord  = binding.toObject(myInput.getValue());
		        ByteBuffer temp = (ByteBuffer) inputRecord.get(0);
				byte[] datatRecords = (byte[]) temp.array();
				ByteArrayInputStream bi = new ByteArrayInputStream( datatRecords );
		        ObjectInputStream in = new ObjectInputStream(bi);
		        data = (Object) in.readObject();
			} catch (IOException e) 
			{
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
				return null;
			}
			return data;
		}
		else 
		{
			System.out.println("Not an acceplatble Memebrane element to retrive");
			return null;
		}
	}
	
	public void storeTempElement(String membrane , String ruleID, String membraneApply, boolean using, Object data) 
	{	
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try 
		{
			out = new ObjectOutputStream(bo);
			out.writeObject(data);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		List<String> minorComponents = Arrays.asList(membrane, ruleID, Boolean.toString(using), membraneApply );
		Key myKey = Key.createKey("temp", minorComponents);    
        GenericData.Record record = new GenericData.Record(schema);
        ByteBuffer buffer = ByteBuffer.wrap((byte []) bo.toByteArray());
        record.put("data", buffer);
        Value KVvalue = binding.toValue(record);
        store.put(myKey, KVvalue);  
	}
	
	public  MultiMembraneMultiset retriveTempElement(String membrane,String ruleID,boolean using) 
	{
		MultiMembraneMultiset data = new  MultiMembraneMultiset();
		//List<Object> data = new ArrayList<Object>();
		//List<String> membranes = new ArrayList<String>();
		try 
		{
			List<String> minorComponents = Arrays.asList(membrane,ruleID, Boolean.toString(using));
			Key myKey = Key.createKey("temp",minorComponents); 
	        SortedMap<Key, ValueVersion> myInput = store.multiGet(myKey, null, null);
	        if (myInput==null)
	        	return null;
			for(Entry<Key, ValueVersion> anEntry: myInput.entrySet())
			{
		        GenericRecord inputRecord = new GenericData.Record(schema);
		        inputRecord  = binding.toObject(anEntry.getValue().getValue());
		        ByteBuffer temp = (ByteBuffer) inputRecord.get(0);
		        byte[] datatRecords = (byte[]) temp.array();
				ByteArrayInputStream bi = new ByteArrayInputStream( datatRecords );
		        ObjectInputStream in = new ObjectInputStream(bi);
		        String membraneApply = anEntry.getKey().getMinorPath().get(3);
				data.add(membraneApply, (int []) in.readObject());
			}
		} catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
			return null;
		}
		return data;
	}

	@Override
	public void storeAlphbaet(String[] data) 
	{
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try 
		{
			out = new ObjectOutputStream(bo);
			out.writeObject(data);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		Key myKey = Key.createKey("alphabet");    
        GenericData.Record record = new GenericData.Record(schema);
        ByteBuffer buffer = ByteBuffer.wrap((byte []) bo.toByteArray());
        record.put("data", buffer);
        Value KVvalue = binding.toValue(record);
        store.put(myKey, KVvalue);  
	}

	@Override
	public String[] retriveAlphbaet() 
	{
		String[] aphabet;
		try 
		{
			Key myKey = Key.createKey("alphabet");
	        ValueVersion myInput = store.get(myKey);
	        GenericRecord inputRecord = new GenericData.Record(schema);
	        inputRecord  = binding.toObject(myInput.getValue());
	        ByteBuffer temp = (ByteBuffer) inputRecord.get(0);
			byte[] aphabetRecords = (byte[]) temp.array();
			ByteArrayInputStream bi = new ByteArrayInputStream( aphabetRecords );
	        ObjectInputStream in = new ObjectInputStream(bi);
	        aphabet = (String[]) in.readObject();
		} catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
			return null;
		}
		return aphabet;

	}

	@Override
	public void storeMembraneList(String[] data) 
	{
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try 
		{
			out = new ObjectOutputStream(bo);
			out.writeObject(data);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
        Key myKey = Key.createKey("membranes");    
        GenericData.Record record = new GenericData.Record(schema);
        ByteBuffer buffer = ByteBuffer.wrap((byte []) bo.toByteArray());
        record.put("data", buffer);
        Value KVvalue = binding.toValue(record);
        store.put(myKey, KVvalue);
	}

	@Override
	public String[] retriveMembraneList()
	{
		String[] aphabet;
		try 
		{
			Key myKey = Key.createKey("membranes");
	        ValueVersion myInput = store.get(myKey);
	        GenericRecord inputRecord = new GenericData.Record(schema);
	        inputRecord  = binding.toObject(myInput.getValue());
	        ByteBuffer temp = (ByteBuffer) inputRecord.get(0);
			byte[] aphabetRecords = (byte[]) temp.array();
			ByteArrayInputStream bi = new ByteArrayInputStream( aphabetRecords );
	        ObjectInputStream in = new ObjectInputStream(bi);
	        aphabet = (String[]) in.readObject();
		} catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
			return null;
		}
		return aphabet;

	}

	@Override
	public void StoreNode(String NodeId, int level, String membrane, NodeData theNode) 
	{
		ByteArrayOutputStream boMultiset = null;
		ByteArrayOutputStream boRules = null;
		try 
		{
			boMultiset = new ByteArrayOutputStream();
			boRules = new ByteArrayOutputStream();
			ObjectOutputStream outMultiset = new ObjectOutputStream(boMultiset);
			ObjectOutputStream outRules = new ObjectOutputStream(boRules);
            outMultiset.writeObject(theNode.multiset);
            outRules.writeObject(theNode.rules);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		List<String> majorComponents = Arrays.asList(Integer.toString(level),membrane);
		Key myKey = Key.createKey(majorComponents, NodeId);    
        GenericData.Record record = new GenericData.Record(nodeSchema);
        ByteBuffer buffer = ByteBuffer.wrap((byte []) boMultiset.toByteArray());
        ByteBuffer buffer2 = ByteBuffer.wrap((byte []) boRules.toByteArray());
        record.put("multiset", buffer);
        record.put("rules",buffer2);
        record.put("duplicate", "");
        record.put("parent", theNode.parent);
        Value KVvalue = nodeBinding.toValue(record);
        store.put(myKey, KVvalue ); 

	}
	
	
	public Value createNodeValue(NodeData theNode)
	{
		ByteArrayOutputStream boMultiset = null;
		ByteArrayOutputStream boRules = null;
		try 
		{
			boMultiset = new ByteArrayOutputStream();
			boRules = new ByteArrayOutputStream();
			ObjectOutputStream outMultiset = new ObjectOutputStream(boMultiset);
			ObjectOutputStream outRules = new ObjectOutputStream(boRules);
            outMultiset.writeObject(theNode.multiset);
            outRules.writeObject(theNode.rules);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
        GenericData.Record record = new GenericData.Record(nodeSchema);
        ByteBuffer buffer = ByteBuffer.wrap((byte []) boMultiset.toByteArray());
        ByteBuffer buffer2 = ByteBuffer.wrap((byte []) boRules.toByteArray());
        record.put("multiset", buffer);
        record.put("rules",buffer2);
        record.put("duplicate", "");
        record.put("parent", theNode.parent);
        Value KVvalue = nodeBinding.toValue(record);
        return KVvalue;
	}

	@Override
	public NodeData RetrieveNode(String NodeId, int level, String membrane) 
	{
		List<String> majorComponents = Arrays.asList(Integer.toString(level),membrane);
		Key myKey = Key.createKey(majorComponents, NodeId);
        ValueVersion myInput = store.get(myKey);
        if (myInput ==null)
        	return null;
        GenericRecord inputRecord = new GenericData.Record(nodeSchema);
        inputRecord  = nodeBinding.toObject(myInput.getValue());
        NodeData aNode = new NodeData();
        
		byte[] multiset = ((ByteBuffer) inputRecord.get(0)).array();
		byte[] rules = ((ByteBuffer) inputRecord.get(1)).array();
		aNode.parent = inputRecord.get(2).toString();
		aNode.duplicate = inputRecord.get(3).toString();
		
		ByteArrayInputStream biMultiset = new ByteArrayInputStream(multiset);
		ByteArrayInputStream biRules = new ByteArrayInputStream(rules);

        ObjectInputStream in;
		try 
		{
			in = new ObjectInputStream(biMultiset);
			aNode.multiset = (int[]) in.readObject();
			in = new ObjectInputStream(biRules);
			aNode.rules = (int[]) in.readObject();

		} catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
			return null;
		}

		return aNode;
	}
	
	@Override
	public List<String> retriveLevelIDs(int level, String membrane) 
	{

		List<String> nodesList = new ArrayList<String>();
		
		List<String> majorComponents = Arrays.asList(Integer.toString(level),membrane);
		Key myKey = Key.createKey(majorComponents);
        SortedSet<Key> myInput = store.multiGetKeys(myKey, null, null);
        if (myInput ==null)
        	return null;
        for (Key aKey : myInput)
        {
        	nodesList.add(aKey.getMinorPath().get(0));
        	
        }     
		return nodesList;
	}

	@Override
	public void deleteLevel(int level) 
	{
		String[] membranes = retriveMembraneList();
        for(int i=0;i<membranes.length;i++)
        {
    		Key myKey = Key.createKey( Arrays.asList(Integer.toString(level),membranes[i]) );
    		store.multiDelete(myKey, null, null);
        }	
	}

	@Override
	public void deletePsystem() 
	{
		deleteLevel(0);
		Key myKey;
		String[] membranes = retriveMembraneList();
        for(int i=0;i<membranes.length;i++)
        {
    		myKey = Key.createKey( Arrays.asList("membrane",membranes[i]) );
    		store.multiDelete(myKey, null, null);
        }	
		myKey = Key.createKey("membranes");
		store.multiDelete(myKey, null, null);
		myKey = Key.createKey("alphabet");
		store.multiDelete(myKey, null, null);
	}
	
	@Override
	public void deleteAllTemp()
	{
		Key myKey;
		myKey = Key.createKey("temp");
		store.multiDelete(myKey, null, null);
	}

	@Override
	public void printLevelSize(int level) 
	{
		Key myKey =  Key.createKey(Integer.toString(level));
		int numElements=0;
		Iterator<Key> myItterator = store.storeKeysIterator( Direction.UNORDERED,0,myKey,null,null);
		while (myItterator.hasNext())
		{
			numElements++;
			myItterator.next();
			//if (numElements % 200000 == 0)
			//	System.out.println("Progress: " + numElements);
		}
		System.out.println("There are: " + numElements + " nodes at level: " + level );		
	}

	@Override
	public void printAllElements() 
	{
		Iterator<KeyValueVersion> myItterator = store.storeIterator( Direction.UNORDERED,0,null, null, null);
		while (myItterator.hasNext())
		{
			System.out.println(myItterator.next().getKey());
		}
		
	}
}
