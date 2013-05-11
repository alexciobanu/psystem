package AdminTools;

import java.util.ArrayList;

import Interfaces.AbstractDatabase;
import Interfaces.MultiMembraneMultiset;
import Interfaces.OracleNoSQLDatabase;

public class FindCdrcPairs 
{
	public static void main(String[] args) 
	{
		FindCdrcPairs cdc = new FindCdrcPairs("kvstore","Desktop:5000");
		cdc.getContextDependentRules();
	}
	
	AbstractDatabase db;
	
	public FindCdrcPairs(String storeName, String hosts)
	{
		db = new OracleNoSQLDatabase(storeName, hosts);
	}

	@SuppressWarnings("unchecked")
	public void getContextDependentRules()
	{
		String[] membranes = db.retriveMembraneList();
		ArrayList<MultiMembraneMultiset> rules;
		int innerCounter,i,j;
		int[] multiset;
		int[] innerMultiset;
		int leftRuleNumber, rightRuleNumber;
		ArrayList<MultiMembraneMultiset> rightRules;
        for(i=0;i<membranes.length;i++)
        {
        	leftRuleNumber=0;
        	rules = (ArrayList<MultiMembraneMultiset>) db.retriveMembraneElement(membranes[i], "left");
	        for(MultiMembraneMultiset aRule : rules)
	        {
	        	multiset = aRule.getMulisetForMembrane(membranes[i]);
	        	//System.out.println( Arrays.toString(multiset) + " " + leftRuleNumber + "_" + membranes[i] );
        		for(j=0;j<membranes.length;j++)
                {
                	rightRules = (ArrayList<MultiMembraneMultiset>) db.retriveMembraneElement(membranes[j], "right");
                	rightRuleNumber=0;
                	for(MultiMembraneMultiset aRightRule : rightRules)
        	        {
                		if (aRightRule.getMembranes().contains(membranes[i]))
                		{
                			//System.out.print("\t");
                			innerMultiset = aRightRule.getMulisetForMembrane(membranes[i]);
                			for(innerCounter=0;innerCounter<innerMultiset.length;innerCounter++)
            	        	{
                				if ( (innerMultiset[innerCounter]>0) && (multiset[innerCounter]>0) )	
                				{
                					if(innerMultiset[innerCounter]>=multiset[innerCounter])
                					{
                						db.storeCDRCPair(rightRuleNumber, membranes[j], leftRuleNumber, membranes[i]);
                						//System.out.println(membranes[j] + "_" + rightRuleNumber + " " + membranes[i] + "_" +leftRuleNumber); 
                						break;
                					}
                				}
                				//System.out.print( innerMultiset[innerCounter] + " ");
            	        	}
                			//System.out.println("_" + rightRuleNumber + "_" + membranes[j] + " ");
                		}
                		rightRuleNumber++;
        	        }
                }
        	leftRuleNumber++;
	        }
        }
	}
}
