package Experiments;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.UUID;

import org.gcn.plinguacore.parser.AbstractParserFactory;
import org.gcn.plinguacore.parser.input.InputParser;
import org.gcn.plinguacore.parser.input.InputParserFactory;
import org.gcn.plinguacore.util.MultiSet;
import org.gcn.plinguacore.util.psystem.Psystem;
import org.gcn.plinguacore.util.psystem.membrane.Membrane;
import org.gcn.plinguacore.util.psystem.rule.IRule;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;

public class KV {

	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		KVStoreConfig config = new KVStoreConfig("kvstore", "localhost:5000");
        KVStore store = KVStoreFactory.getStore(config);
        
        //grabPsystem();

        //list alphabet
        ValueVersion aphabetRecords = store.get(Key.createKey("alphabet"));
      
		ByteArrayInputStream bi2 = new ByteArrayInputStream( aphabetRecords.getValue().getValue() );
        ObjectInputStream in2 = new ObjectInputStream(bi2);
        Object[] aphabet = (Object[]) in2.readObject();
        for(int i=0;i<aphabet.length;i++)
        {
        	 System.out.print(aphabet[i]);
        }
        System.out.println();
        
        //list membranes
        ValueVersion membranesRecords = store.get(Key.createKey("membranes"));
      
		ByteArrayInputStream bi4 = new ByteArrayInputStream(  membranesRecords.getValue().getValue() );
        ObjectInputStream in4 = new ObjectInputStream(bi4);
        String[] membranes = (String[]) in4.readObject();
        for(int i=0;i<membranes.length;i++)
        {
        	 System.out.print(membranes[i]);
        }
        System.out.println();
        
        //list initial multisets
        SortedMap<Key, ValueVersion> myRecords = null;
        myRecords = store.multiGet(Key.createKey("level0"), null, null);

        
        for (Map.Entry<Key, ValueVersion> entry : myRecords.entrySet() ) 
    	{
    	    Value v = entry.getValue().getValue();
    	    //Key k = entry.getKey();
    	    ByteArrayInputStream bi = new ByteArrayInputStream(v.getValue());
            ObjectInputStream in = new ObjectInputStream(bi);
            long[] array = (long[]) in.readObject();
            //System.out.println(k.getMinorPath());
            for (int i=0;i<array.length;i++)
			{
				System.out.print(array[i]);
			}
			System.out.println();	
    	}
        
        //list all rules
        SortedMap<Key, ValueVersion> myRules = null;
        myRules = store.multiGet(Key.createKey("rules"), null, null);
        for (Map.Entry<Key, ValueVersion> entry : myRules.entrySet() ) 
    	{
    	    Value v = entry.getValue().getValue();
    	    Key k = entry.getKey();
    	    ByteArrayInputStream bi = new ByteArrayInputStream(v.getValue());
            ObjectInputStream in = new ObjectInputStream(bi);
            @SuppressWarnings("unchecked")
			ArrayList<long []> rules = (ArrayList<long []>) in.readObject();
			System.out.println(k.toString());
			for (long[] bla : rules)
	        {
	        	for(int k1=0;k1<bla.length;k1++)
	        	{
	        		
	        		System.out.print(bla[k1]);
	        	}
	        	System.out.println();
	        }
    	}
        
     store.close();
	}
	
	static void grabPsystem()
	{
		try
		{
		//FileInputStream stream = new FileInputStream("C:\\Users\\a\\workspace\\Simulator\\src\\p2.pli");
		FileInputStream stream = new FileInputStream("/home/a/Workspace/Simulator/src/Psystem.pli");
		AbstractParserFactory pf = new InputParserFactory();
		InputParser parser = (InputParser) pf.createParser("P-Lingua");
		
		KVStoreConfig config = new KVStoreConfig("kvstore", "localhost:5000");
        KVStore store = KVStoreFactory.getStore(config);
		Psystem ps = parser.parse(stream);
		
		//Alphabet
		Object[] alphabet = ps.getAlphabet().toArray();
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bo);
        out.writeObject(alphabet);
		String mComponent = "alphabet";
		Key aKey = Key.createKey( mComponent);
		Value aValue = Value.createValue(bo.toByteArray());
		store.put(aKey,aValue);
		
		//Membranes
		String[] membranes = new String[ps.getMembraneStructure().getAllMembranes().size()-1];
		int i=0;
		for (Membrane bla : ps.getMembraneStructure().getAllMembranes() )
		{
			if  (bla.getLabel().compareTo("0")!=0)
				{
					membranes[i]= bla.getLabel();
					i++;
				}	
		}
		ByteArrayOutputStream bo3 = new ByteArrayOutputStream();
		ObjectOutputStream out3 = new ObjectOutputStream(bo3);
        out3.writeObject(membranes);
		String majComponent = "membranes";
		Key theKey = Key.createKey( majComponent);
		Value theValue = Value.createValue(bo3.toByteArray());
		store.put(theKey,theValue);
		
		//Initial Multiset
		int alphabetSize = alphabet.length;	
		String uuid = UUID.randomUUID().toString();
		for(Membrane a : ps.getMembraneStructure().getAllMembranes())
		{
			MultiSet<String> initialMultiset= ps.getInitialMultiSets().get(a.getLabel());
			if (initialMultiset==null)
				break;
			long objects[] = new long[alphabetSize];
			for(i=0;i<alphabetSize;i++)
			{	
				objects[i]=initialMultiset.count(alphabet[i].toString());
			}
			ByteArrayOutputStream bo2 = new ByteArrayOutputStream();
			ObjectOutputStream out2 = new ObjectOutputStream(bo2);
	        out2.writeObject(objects);
			String majorComponent = "level0";
			List<String> minorComponents = Arrays.asList(uuid,a.getLabel());
			Key myKey = Key.createKey( majorComponent,minorComponents);
			Value value = Value.createValue(bo2.toByteArray());
			store.put(myKey,value);
		}
		
		//Rules raw
		int j;
		for (IRule aRule : ps.getRules())	
		{
			long [] arule = new long[alphabetSize];
			Membrane currentMembrane = aRule.getLeftHandRule().getOuterRuleMembrane();
			for(j=0;j<alphabetSize;j++)
			{
				arule[j]=currentMembrane.getMultiSet().count(alphabet[j].toString());
			}
			ByteArrayOutputStream bo2 = new ByteArrayOutputStream();
			ObjectOutputStream out2 = new ObjectOutputStream(bo2);
	        out2.writeObject(arule);
	        String membrane = aRule.getLeftHandRule().getOuterRuleMembrane().getLabel();
	        List<String> majorComponent = Arrays.asList("tempRules");
	        String uuid2 = UUID.randomUUID().toString();
	        List<String> minorComponent = Arrays.asList(membrane,uuid2);
			Key myKey = Key.createKey( majorComponent,minorComponent);
			Value value = Value.createValue(bo2.toByteArray());
			store.put(myKey,value);
		}
		
		//Rules Sorted
		
        for (i=0;i<membranes.length;i++) 
        {
        	//System.out.println("");
			SortedMap<Key, ValueVersion> myRules = null;
			myRules = store.multiGet(Key.createKey("tempRules", membranes[i]),null, null);
			ArrayList<long[]> aggregatedRules = new ArrayList<long[]>();
			for (Map.Entry<Key, ValueVersion> entry : myRules.entrySet()) 
			{
				Value v = entry.getValue().getValue();
				ByteArrayInputStream bi = new ByteArrayInputStream(v.getValue());
				ObjectInputStream in = new ObjectInputStream(bi);
				long[] array = (long[]) in.readObject();
				aggregatedRules.add(array);
			}
			ByteArrayOutputStream bo4 = new ByteArrayOutputStream();
			ObjectOutputStream out4 = new ObjectOutputStream(bo4);
			out4.writeObject(aggregatedRules);
			Key rulesKey = Key.createKey("rules", membranes[i]);
			Value rulesValue = Value.createValue(bo4.toByteArray());
			store.put(rulesKey, rulesValue);
		}
		
		
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

}
