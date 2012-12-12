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

import Importer.PsystemImporter;
import Simulator.NodeCalculator;

import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.avro.AvroCatalog;
import oracle.kv.avro.GenericAvroBinding;

public class MainClass 
{
	static Key key;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		PsystemImporter ps = new PsystemImporter();
		ps.grabPsystem("/home/cloudera/workspace/Simulator/src/p2.pli");

		GenericRecord entry = getRecord();
	    Key k2 = key;
	    String membrane = (k2.getMajorPath()).get(1);
	    String uuid = (k2.getMinorPath()).get(0);
	    
        ByteBuffer temp = (ByteBuffer) entry.get(0);
	    ByteArrayInputStream bi2 = new ByteArrayInputStream(temp.array());
        ObjectInputStream in2 = new ObjectInputStream(bi2);
        long[] currentMultiset = (long[]) in2.readObject();
        
        NodeCalculator nc = new NodeCalculator(membrane);
        nc.getAllCombinations(currentMultiset,"level1",uuid);
        
        ps.printChildren(uuid);
	    ps.printAlphabet();
	    ps.printMembranes();
		ps.printAllRules();
        ps.printMultiset("level0");
        ps.printMultiset("level1");
	}
	
	static GenericRecord getRecord() throws IOException
	{
		KVStoreConfig config = new KVStoreConfig("kvstore", "localhost:5000");
		KVStore store = KVStoreFactory.getStore(config);
        Iterator<KeyValueVersion> levelIterator = store.storeIterator(Direction.UNORDERED, 0, Key.createKey("level0"), null, null);
        AvroCatalog catalog = store.getAvroCatalog(); 
        File f = new File("/home/cloudera/kv-2.0.15/schema.avsc");
        Schema.Parser parser = new Schema.Parser();
        parser.parse(f);
        Schema schema = parser.getTypes().get("dataRecord"); 
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
