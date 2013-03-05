package CDRCTesting;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Interfaces.DatabaseAccess;

public class FindCdrcPairs 
{
	DatabaseAccess db;
	
	FindCdrcPairs(String storeName, String hosts)
	{
		db = new DatabaseAccess(storeName, hosts);
	}
	
	void getContextDependentRules()
	{

		byte[] membranesRecords = (byte[]) db.retrieve("membranes", null); 
		ByteArrayInputStream bi = new ByteArrayInputStream(  membranesRecords );
        ObjectInputStream in;
		try {
			in = new ObjectInputStream(bi);
	        String[] membranes = (String[]) in.readObject();
	        for(int i=0;i<membranes.length;i++)
	        {
	        	byte[] myRules = (byte[]) db.retrieve("rules", membranes[i]);
		    	bi = new ByteArrayInputStream(myRules);
		        in = new ObjectInputStream(bi);
		        @SuppressWarnings("unchecked")
		        ArrayList<long [][]> rules = (ArrayList<long [][]>) in.readObject();
		        for(int ruleCounter = 0; ruleCounter<rules.size();ruleCounter++)
		        {
		        	long[][] currentRule= rules.get(ruleCounter);
		        	for(int j=0;j<currentRule[1].length;j++)
		        	{
		        		if(currentRule[1][j]>0)
		        		{
		        			for(int rule2Counter = 0; rule2Counter<rules.size();rule2Counter++)
		        			{
		        				long[][] current2Rule= rules.get(rule2Counter);
		        				if(current2Rule[0][j]>0)
		        				{
		        					List<String> cdrcPair = Arrays.asList(Integer.toString(ruleCounter) ,(Integer.toString(rule2Counter)));	        					
		        					db.store(Arrays.asList("CDRCrules",membranes[i]), cdrcPair,new byte[0]);
		        					//System.out.println(ruleCounter+" "+rule2Counter);
		        				}
		        			}
		        			
		        		}
		        	}	
		        }
	        }
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
}
