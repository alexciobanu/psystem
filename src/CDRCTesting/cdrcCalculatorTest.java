package CDRCTesting;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.avro.AvroCatalog;
import oracle.kv.avro.GenericAvroBinding;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import Interfaces.DatabaseAccess;
import Interfaces.NodeData;

public class cdrcCalculatorTest 
{
	static Key key;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		GenericRecord record = getRecord();
		Key keyArg = key;
		String level = (keyArg.getMajorPath()).get(0);
	    String membrane = (keyArg.getMajorPath()).get(1);
	    String uuid = (keyArg.getMinorPath()).get(0);
	    
		int buff = Integer.parseInt( level.substring(5, level.length()) );
		buff++;
		String nextLevel= "level"+buff;
	    
        ByteBuffer temp = (ByteBuffer) record.get(1);
	    ByteArrayInputStream bi2 = new ByteArrayInputStream(temp.array());
        ObjectInputStream in2 = new ObjectInputStream(bi2);
        long[] currentRule = (long[]) in2.readObject();

                
        ArrayList<long[]> allChildren = getChildren(uuid,nextLevel,membrane);
        DatabaseAccess db = new DatabaseAccess();
        List<String> majorComponent = Arrays.asList("CDRCrules",membrane);
        for(long[] aChild: allChildren)
        {
        	for(int i=0;i<currentRule.length;i++)
        	{
        		if (currentRule[i]>0)
        		{
        			for(int j=0;j<aChild.length;j++)
        			{
        				if (aChild[j]>0)
        				{
        					List<String> cdrcPair = Arrays.asList(Integer.toString(i) ,(Integer.toString(j)));
        					if( db.retrieveWithNull(majorComponent,cdrcPair) != null )
        					{
        						String output = new String();
        						output += cdrcPair + ": ";
        						output += getParentRules(level,membrane,uuid);
        						output += Arrays.toString(currentRule) + ", " + Arrays.toString(aChild);
        						db.delete(majorComponent,cdrcPair);
        						System.out.println(output);
        					}
        					
        				}
        			}
        		}
        	}
        }
	}
	
	public static String getParentRules(String level, String membrane, String uuid) throws IOException, ClassNotFoundException
	{
		
		int levelNumber = Integer.parseInt( level.substring(5, level.length()) );
		String output = new String();
		while(levelNumber>1)
		{	
			String curentLevel= "level"+levelNumber;
			List<String> majorComponent = Arrays.asList(curentLevel, membrane);
			DatabaseAccess db = new DatabaseAccess();
			NodeData temp = db.retrieveNode(majorComponent,uuid);
			String parent = temp.parent;
		
			levelNumber--;
			String previousLevel= "level"+levelNumber;
			
			majorComponent = Arrays.asList(previousLevel, membrane);
			NodeData parentNode = db.retrieveNode(majorComponent, parent);
			byte[] rules = parentNode.rules;
			ByteArrayInputStream bi2 = new ByteArrayInputStream(rules);
	        ObjectInputStream in2 = new ObjectInputStream(bi2);
			long[] rulesArray = (long[]) in2.readObject();
			output+= Arrays.toString( rulesArray) + ", ";
		}
		return output;
		
	}
	
	public static ArrayList<long[]> getChildren(String uuid,String level, String membrane) throws IOException, ClassNotFoundException
	{
		ArrayList<long[]> allChildren = new ArrayList<long[]>();
		DatabaseAccess db = new DatabaseAccess();
		byte[] temp = db.retrieve(uuid,null);
		ByteArrayInputStream bi2 = new ByteArrayInputStream(temp);
        ObjectInputStream in2 = new ObjectInputStream(bi2);
        @SuppressWarnings("unchecked")
		ArrayList<String> children = (ArrayList<String>) in2.readObject();
        for(int i=0;i<children.size();i++)
        {
        	long[] childRules = getChildRules(children.get(i),level,membrane);
        	allChildren.add(childRules);
        }
        return allChildren;
	}
	
	public static long[] getChildRules(String uuid,String level, String membrane) throws IOException, ClassNotFoundException
	{
		List<String> majorComponents = Arrays.asList(level,membrane);
		DatabaseAccess db = new DatabaseAccess();
		NodeData temp = db.retrieveNode(majorComponents,uuid);
		byte[] rules = temp.rules;
		ByteArrayInputStream bi2 = new ByteArrayInputStream(rules);
        ObjectInputStream in2 = new ObjectInputStream(bi2);
		long[] rulesArray = (long[]) in2.readObject();
		return rulesArray;
	}
	
	static GenericRecord getRecord() throws IOException
	{
		KVStoreConfig config = new KVStoreConfig("kvstore", "localhost:5000");
		KVStore store = KVStoreFactory.getStore(config);
        Iterator<KeyValueVersion> levelIterator = store.storeIterator(Direction.UNORDERED, 0, Key.createKey("level1"), null, null);
        AvroCatalog catalog = store.getAvroCatalog(); 
        File f = new File("/home/a/kv-2.0.23/node.avsc");
        Schema.Parser parser = new Schema.Parser();
        parser.parse(f);
        Schema schema = parser.getTypes().get("nodeRecord"); 
        GenericAvroBinding binding = catalog.getGenericBinding(schema);
        while( levelIterator.hasNext())
		{
	        GenericRecord inputRecord = new GenericData.Record(schema);
	        KeyValueVersion bla = levelIterator.next();
	        inputRecord  = binding.toObject(bla.getValue());
	        key = bla.getKey();
			return  inputRecord;
		}
		return null;
	}
}
