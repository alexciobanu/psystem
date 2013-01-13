package Experiments;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import AdminTools.PsystemInterface;
import Simulator.NodeCalculator;

import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.avro.AvroCatalog;
import oracle.kv.avro.GenericAvroBinding;

public class DerivationTreeTest 
{
	static Key key;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		PsystemInterface ps = new PsystemInterface();

		GenericRecord entry = getRecord();
	    Key k2 = key;
	    String membrane = (k2.getMajorPath()).get(1);
	    String uuid = (k2.getMinorPath()).get(0);
	    
        ByteBuffer temp = (ByteBuffer) entry.get(0);
	    ByteArrayInputStream bi2 = new ByteArrayInputStream(temp.array());
        ObjectInputStream in2 = new ObjectInputStream(bi2);
        long[] currentMultiset = (long[]) in2.readObject();
        
        NodeCalculator nc = new NodeCalculator(membrane);
        nc.getAllCombinations(currentMultiset,"level2",uuid);
        
        ps.printChildren(uuid);
	    ps.printAlphabet();
	    ps.printMembranes();
		ps.printAllRules();
        ps.printMultiset("level0");
        ps.printLevelSize("level0");
		ps.printMultiset("level1");
        ps.printLevelSize("level1");
		ps.printMultiset("level2");
        ps.printLevelSize("level2");
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
