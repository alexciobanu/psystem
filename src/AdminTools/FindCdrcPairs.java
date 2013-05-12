package AdminTools;

import java.util.ArrayList;

import Interfaces.AbstractDatabase;
import Interfaces.MultiMembraneMultiset;

public class FindCdrcPairs 
{
	/*public static void main(String[] args) 
	{
		AbstractDatabase db = new OracleNoSQLDatabase("PsystemStore","hadoop1:5000");
		FindCdrcPairs cdc = new FindCdrcPairs();
		cdc.getContextDependentRules(db);
	}*/
	
	public FindCdrcPairs()
	{
		
	}

	@SuppressWarnings("unchecked")
	public void getContextDependentRules(AbstractDatabase db)
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
