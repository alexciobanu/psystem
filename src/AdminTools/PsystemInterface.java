package AdminTools;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.gcn.plinguacore.parser.AbstractParserFactory;
import org.gcn.plinguacore.parser.input.InputParser;
import org.gcn.plinguacore.parser.input.InputParserFactory;
import org.gcn.plinguacore.util.MultiSet;
import org.gcn.plinguacore.util.psystem.Psystem;
import org.gcn.plinguacore.util.psystem.membrane.Membrane;
import org.gcn.plinguacore.util.psystem.rule.IRule;

import Interfaces.DatabaseAccess;
import Interfaces.NodeData;


public class PsystemInterface 
{
	DatabaseAccess db;
	
	public PsystemInterface(String storeName, String hosts)
	{
		db= new DatabaseAccess(storeName,hosts);
	}
	
	public void close()
	{
		//store.close();
	}
	
	public void printAlphabet()
	{
		try 
		{
			byte[] aphabetRecords = (byte[]) db.retrieve("alphabet", null);
			ByteArrayInputStream bi2 = new ByteArrayInputStream( aphabetRecords );
	        ObjectInputStream in2 = new ObjectInputStream(bi2);
	        Object[] aphabet = (Object[]) in2.readObject();
	        for(int i=0;i<aphabet.length;i++)
	        {
	        	 System.out.print(aphabet[i]);
	        }
	        System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void printLevelSize(String level)  
	{
		int number = db.countObjects(level);
		System.out.println(number);
	}
	
	public void printAllKeys()  
	{
		db.printAllKeys();
	}
	
	public void deleteCDRC()  
	{
		byte[] membranesRecords = (byte[]) db.retrieve("membranes", null); 
		ByteArrayInputStream bi = new ByteArrayInputStream(  membranesRecords );
        ObjectInputStream in;
        String[] membranes = null;
		try 
		{
			in = new ObjectInputStream(bi);
			membranes = (String[]) in.readObject();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
        for(int i=0;i<membranes.length;i++)
        {
        	db.multiDelete( Arrays.asList("CDRCrules",membranes[i]) ); 
        }
	}
	
	public void deleteLevel(String level)  
	{
		byte[] membranesRecords = (byte[]) db.retrieve("membranes", null); 
		ByteArrayInputStream bi = new ByteArrayInputStream(  membranesRecords );
        ObjectInputStream in;
        String[] membranes = null;
		try 
		{
			in = new ObjectInputStream(bi);
			membranes = (String[]) in.readObject();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
        for(int i=0;i<membranes.length;i++)
        {
        	db.multiDelete( Arrays.asList(level,membranes[i]) ); 
        }
	}
	
	public void deletePsystem()  
	{
		deleteLevel("level0");
		deleteCDRC();
		db.multiDelete("tempRules");
		db.multiDelete("rules");
		db.multiDelete("membranes");
		db.multiDelete("alphabet");
		db.multiDelete("tempRules");
		
	}
	
	public void printChildren(String parent)
	{
		try
		{
			byte[] membranesRecords = (byte[]) db.retrieve(parent, null);
	    	ByteArrayInputStream bi = new ByteArrayInputStream(membranesRecords);
	        ObjectInputStream in = new ObjectInputStream(bi);
	        @SuppressWarnings("unchecked")
	        ArrayList<String> array = (ArrayList<String>) in.readObject();
	        for (String child : array)
			{
				System.out.print(child+" ");
			}
			System.out.println();	
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void printMembranes()  
	{
		try
		{
			byte[] membranesRecords = (byte[]) db.retrieve("membranes", null); 
			ByteArrayInputStream bi = new ByteArrayInputStream(  membranesRecords );
	        ObjectInputStream in = new ObjectInputStream(bi);
	        String[] membranes = (String[]) in.readObject();
	        for(int i=0;i<membranes.length;i++)
	        {
	        	 System.out.print(membranes[i]);
	        }
	        System.out.println();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void printMultiset(String level)  
	{
		List<NodeData> myMultiset;
        myMultiset = db.multiRetrieveNodeSchema(level);
		for(NodeData aMultiset : myMultiset)
		{
    	    ByteArrayInputStream bi = new ByteArrayInputStream(aMultiset.data);
    	    ByteArrayInputStream bi2 = new ByteArrayInputStream(aMultiset.rules);
            ObjectInputStream in;
            ObjectInputStream in2;
            long[] data=null;
            long[] rules=null;
			try 
			{
				in = new ObjectInputStream(bi);
				in2 = new ObjectInputStream(bi2);
				data = (long[]) in.readObject();
				rules = (long[]) in2.readObject();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} 
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
            for (int i=0;i<data.length;i++)
			{
				System.out.print(data[i]);
			}
            System.out.print(" - ");
            for (int i=0;i<rules.length;i++)
			{
				System.out.print(rules[i]);
			}
            System.out.print(" - " + aMultiset.parent);
			System.out.println();	
		}
	
	}

	
	public void printAllRules()  throws IOException, ClassNotFoundException
	{
		try
		{
			List<byte[]> myRules;
	        myRules =  db.multiRetrieve("rules",null);
	        for (byte[] entry : myRules ) 
	    	{
	    	    //Value v = entry.getValue().getValue();
	    	    //Key k = entry.getKey();
	    	    ByteArrayInputStream bi = new ByteArrayInputStream(entry);
	            ObjectInputStream in = new ObjectInputStream(bi);
				@SuppressWarnings("unchecked")
				ArrayList<long [][]> rules = (ArrayList<long [][]>) in.readObject();
				//System.out.println(k.toString());
				for (long[][] bla : rules)
		        {
		        	for(int k1=0;k1<bla[0].length;k1++)
		        	{
		        		
		        		System.out.print(bla[0][k1]);
		        	}
		        	System.out.print("-");
		        	for(int k1=0;k1<bla[1].length;k1++)
		        	{
		        		
		        		System.out.print(bla[1][k1]);
		        	}
		        	
		        	System.out.println();
		        }
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	void printMembraneRules(String Membrane)  
	{
		try
		{
			byte[] myRules = (byte[]) db.retrieve("rules", Membrane);
	    	ByteArrayInputStream bi = new ByteArrayInputStream(myRules);
	        ObjectInputStream in = new ObjectInputStream(bi);
	        @SuppressWarnings("unchecked")
	        ArrayList<long [][]> rules = (ArrayList<long [][]>) in.readObject();
			for (long[][] bla : rules)
		    {
		        for(int k1=0;k1<bla[0].length;k1++)
		        {	
		        	System.out.print(bla[0][k1]);
		        }
	        	System.out.print("-");
	        	for(int k1=0;k1<bla[1].length;k1++)
	        	{
	        		
	        		System.out.print(bla[1][k1]);
	        	}
		        System.out.println();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public void grabPsystem(String fileName)
	{
		try
		{
			FileInputStream stream = new FileInputStream(fileName);
			AbstractParserFactory pf = new InputParserFactory();
			InputParser parser = (InputParser) pf.createParser("P-Lingua");
			
			Psystem ps = parser.parse(stream);
			
			//Alphabet
			Object[] alphabet = ps.getAlphabet().toArray();
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bo);
	        out.writeObject(alphabet);
			db.store("alphabet", null, bo.toByteArray());
			
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
			db.store("membranes", null, bo3.toByteArray());
			
			//Initial Multiset
			int alphabetSize = alphabet.length;	
			
			for(Membrane a : ps.getMembraneStructure().getAllMembranes())
			{
				String uuid = UUID.randomUUID().toString();
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
		        db.storeNode(Arrays.asList("level0",a.getLabel()),uuid,bo2.toByteArray(),new byte[0],"" );
			}
			
			//Rules raw
			int j;
			for (IRule aRule : ps.getRules())	
			{
				long [][] arule = new long[2][alphabetSize];
				Membrane currentMembrane = aRule.getLeftHandRule().getOuterRuleMembrane();
				Membrane currentMembraneRight = aRule.getRightHandRule().getOuterRuleMembrane();
				for(j=0;j<alphabetSize;j++)
				{
					arule[0][j]=currentMembrane.getMultiSet().count(alphabet[j].toString());
					arule[1][j]=currentMembraneRight.getMultiSet().count(alphabet[j].toString());
				}
				ByteArrayOutputStream bo2 = new ByteArrayOutputStream();
				ObjectOutputStream out2 = new ObjectOutputStream(bo2);
		        out2.writeObject(arule);
		        String membrane = aRule.getLeftHandRule().getOuterRuleMembrane().getLabel();
		        String uuid2 = UUID.randomUUID().toString();
				db.store("tempRules", Arrays.asList(membrane,uuid2), bo2.toByteArray());
			}
			
			//Rules Sorted
			i=0;
	        for (i=0;i<membranes.length;i++) 
	        {
				List<byte[]> myRules = null;
				myRules = db.multiRetrieve("tempRules",membranes[i]);
				ArrayList<long[][]> aggregatedRules = new ArrayList<long[][]>();
				for (byte[] entry : myRules ) 
				{
					ByteArrayInputStream bi = new ByteArrayInputStream(entry);
					ObjectInputStream in = new ObjectInputStream(bi);
					long[][] array = (long[][]) in.readObject();
					aggregatedRules.add(array);
				}
				ByteArrayOutputStream bo4 = new ByteArrayOutputStream();
				ObjectOutputStream out4 = new ObjectOutputStream(bo4);
				out4.writeObject(aggregatedRules);
				db.store("rules", membranes[i], bo4.toByteArray());
			}
	        db.multiDelete("tempRules");
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

}
