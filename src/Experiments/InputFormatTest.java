package Experiments;

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
import oracle.kv.ValueVersion;

import org.apache.hadoop.mapreduce.InputSplit;

import InputFormat.NoSQLSplit;

public class InputFormatTest 
{
	public static void main(String[] args) 
	{
		KVStoreConfig config = new KVStoreConfig("PsystemStore", "machine1:5000");
        KVStore store = KVStoreFactory.getStore(config);
		List<InputSplit> inputs = new ArrayList<InputSplit>();
		
		String majorKey = "level0";
		Key myKey = Key.createKey( majorKey );

		Iterator<KeyValueVersion> myItterator = store.storeIterator( Direction.UNORDERED,0,myKey,null,null);
		while (myItterator.hasNext())
		{
			Key key = myItterator.next().getKey();
			key.getMajorPath();
			InputSplit anInput = new NoSQLSplit( key.getMajorPath().get(1), key.getMinorPath().get(0) );
			inputs.add(anInput);   
		}
		
		for(InputSplit anInput: inputs)
		{
			NoSQLSplit aSplit = (NoSQLSplit) anInput;
			List<String> majorPath = Arrays.asList(majorKey,aSplit.membrane);
			Key key = Key.createKey(majorPath,aSplit.uuid);
			System.out.println(key);
			ValueVersion inputData = store.get( key );
			System.out.println(inputData);
		}
	}

}
