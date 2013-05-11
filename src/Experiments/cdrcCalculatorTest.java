package Experiments;

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

import Interfaces.AbstractDatabase;
import Interfaces.DatabaseAccess;
import Interfaces.MultiMembraneMultiset;
import Interfaces.NodeData;
import Interfaces.OracleNoSQLDatabase;

public class cdrcCalculatorTest 
{
	static Key key;
	static DatabaseAccess db;
	static AbstractDatabase db2;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		db2 = new OracleNoSQLDatabase("PsystemStore","hadoop1:5000");
		String[] membranes = db2.retriveMembraneList();
		List<String> IDs = db2.retriveLevelIDs(0, membranes[0]);
		ArrayList<String> allChildren = db2.RetrieveChildren(IDs.get(0));
		db2.RetriveAppliedRules(allChildren.get(0));

	    MultiMembraneMultiset appliedRules = db2.RetriveAppliedRules(allChildren.get(0));
	    
        List<String> majorComponent = Arrays.asList("CDRCrules",membranes[0]);
        /*for(String aChild: allChildren)
        {
        	MultiMembraneMultiset childRules = db2.RetriveAppliedRules(aChild);
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
        }*/
	}
	
	public static String getParentRules(String level, String membrane, String uuid) throws IOException, ClassNotFoundException
	{
		
		int levelNumber = Integer.parseInt( level.substring(5, level.length()) );
		String output = new String();
		while(levelNumber>1)
		{	
			String curentLevel= "level"+levelNumber;
			List<String> majorComponent = Arrays.asList(curentLevel, membrane);
			NodeData temp = db.retrieveNode(majorComponent,uuid);
			String parent = temp.parent;
		
			levelNumber--;
			String previousLevel= "level"+levelNumber;
			
			majorComponent = Arrays.asList(previousLevel, membrane);
			NodeData parentNode = db.retrieveNode(majorComponent, parent);
			int[] rules = parentNode.rules;
			//ByteArrayInputStream bi2 = new ByteArrayInputStream(rules);
	        //ObjectInputStream in2 = new ObjectInputStream(bi2);
			//long[] rulesArray = (long[]) in2.readObject();
			output+= Arrays.toString( rules) + ", ";
		}
		return output;
		
	}
	
	public static ArrayList<int[]> getChildren(String uuid,String level, String membrane) throws IOException, ClassNotFoundException
	{
		ArrayList<int[]> allChildren = new ArrayList<int[]>();
		byte[] temp = db.retrieve(uuid,null);
		ByteArrayInputStream bi2 = new ByteArrayInputStream(temp);
        ObjectInputStream in2 = new ObjectInputStream(bi2);
        @SuppressWarnings("unchecked")
		ArrayList<String> children = (ArrayList<String>) in2.readObject();
        for(int i=0;i<children.size();i++)
        {
        	int[] childRules = getChildRules(children.get(i),level,membrane);
        	allChildren.add(childRules);
        }
        return allChildren;
	}
	
	public static int[] getChildRules(String uuid,String level, String membrane) throws IOException, ClassNotFoundException
	{
		List<String> majorComponents = Arrays.asList(level,membrane);
		NodeData temp = db.retrieveNode(majorComponents,uuid);
		int[] rules = temp.rules;
		//ByteArrayInputStream bi2 = new ByteArrayInputStream(rules);
        //ObjectInputStream in2 = new ObjectInputStream(bi2);
		//int[] rulesArray = (int[]) in2.readObject();
		return rules;
	}
}
