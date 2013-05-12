package Experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cern.colt.Arrays;

import Interfaces.AbstractDatabase;
import Interfaces.MultiMembraneMultiset;
import Interfaces.NodeData;
import Interfaces.OracleNoSQLDatabase;

public class cdrcCalculatorTest 
{
	static AbstractDatabase db;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		db = new OracleNoSQLDatabase("PsystemStore","hadoop1:5000");
		String[] membranes = db.retriveMembraneList();
		List<String> IDs = db.retriveLevelIDs(1, membranes[0]);
		String theID = IDs.get(0);
		MultiMembraneMultiset parentRules = db.RetriveAppliedRules(theID);
		ArrayList<String> allChildren = db.RetrieveChildren(theID);
		int parentMembrane, childMembrane;
		int parentRule, childRule;
		MultiMembraneMultiset childRules;

		for(String aChild: allChildren)
        {
        	childRules = db.RetriveAppliedRules(aChild);
        	parentMembrane = 0;
        	for(int [] multisets : parentRules.getMultisets())
        	{
        		childMembrane = 0;
        		for(int [] childMultiset : childRules.getMultisets())
            	{
        			for(parentRule=0;parentRule<multisets.length;parentRule++)
        			{
        				for(childRule=0;childRule<childMultiset.length;childRule++)
            			{
        					if (db.checkAndRemoveCDRCPair(parentRule, parentMembrane, childRule, childMembrane))
        					{
        						System.out.println( parentRule + "_" +parentMembrane +" --> "+ childRule + "_" + childMembrane );
        					}
            			}
        				
        			}
        		childMembrane++;
            	}
        	parentMembrane++;	
        	}
        }
	}


	public static String getParentRules(int level, String membrane, String uuid) throws IOException, ClassNotFoundException
	{
		String output = new String();
		String[] membranes = db.retriveMembraneList();
		while(level>1)
		{	
			NodeData aNode = db.RetrieveNode(uuid, level, membrane);
			String parrentString = aNode.parent;
			for(String aMembrane: membranes)
			{
				NodeData aNode1 = db.RetrieveNode(uuid, level, aMembrane);
				output+=Arrays.toString(aNode1.multiset);
			}
			uuid = parrentString;
			level--;
		}
		return output;
		
	}
}
	
