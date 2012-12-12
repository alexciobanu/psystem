package Simulator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import Interfaces.DatabaseAccess;

public class NodeCalculator 
{	
	ArrayList<long [][]> membraneRules;
	long[] currentMultiset;
	int numberofRules;
	int alphabetSize;
	String membrane;
	DatabaseAccess db;
	
	@SuppressWarnings("unchecked")
	public NodeCalculator(String theMembrane)
	{
		db=new DatabaseAccess();
		membrane=theMembrane;
		byte[] data = db.retrieve("rules", membrane);
    	ByteArrayInputStream bi = new ByteArrayInputStream(data);
        ObjectInputStream in;
		try 
		{
			in = new ObjectInputStream(bi);
			membraneRules = (ArrayList<long [][]>) in.readObject();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		numberofRules = membraneRules.size();
	}
	
	boolean checkCombination(long[] aCombination)
	{
		long objectsUsed []= new long[alphabetSize];
		int i,j;
		for(i=0;i<alphabetSize;i++)
		{
			objectsUsed[i]=0;
		}
		//calculate usage
		i=0;
		for(long[][] aRule : membraneRules)
		{
			for(j=0;j<alphabetSize;j++)
			{
				objectsUsed[j]+=aRule[0][j]*aCombination[i];
			}
		i++;
		}
		//check if all of the objects are used up
		if (Arrays.equals(currentMultiset,objectsUsed))
		{
			return true;
		}
		//check if one of the objects would need to be over used
		for(i=0;i<alphabetSize;i++)
		{
			if(objectsUsed[i]>currentMultiset[i])
			{
				return false;
			} 
		}
		//check if one of the object is rightly underused
		for(i=0;i<alphabetSize;i++)
		{
			if(objectsUsed[i]<currentMultiset[i])
			{
				/*TODO 
				check if there is a rules that can use that object
				*/
				return false;
			} 
		}
		
		return true;
	}
	
	public int getAllCombinations(long[] aMultiset,String level, String uuid)
	{
		currentMultiset = aMultiset;
		alphabetSize = currentMultiset.length;
		ArrayList<long []> allPossibilities = new ArrayList<long []>();
		
		//get the maximum number of times each rule can be applied
		long[] maxRuleApply = new long[numberofRules];
		int i=0,j;
		for(long[][] aRule : membraneRules)
		{
			long maxApply=Long.MAX_VALUE;
			long apply = 0;
			for(j=0;j<alphabetSize;j++)
			{
				try{ apply=currentMultiset[j]/aRule[0][j]; } catch(Exception e) {apply=Long.MAX_VALUE;}
				if (apply<maxApply) maxApply=apply;
			}
			maxRuleApply[i]=maxApply;
			i++;
		}
		
		long counter[]= new long[numberofRules]; //this is will a counter of the current combination being tried
		boolean breakCondition=false;
		int k;
		
		//go through all possible combinations and see which are maximal
		while(!breakCondition)
		{	
			long[] aCombination = new long[numberofRules];
			for(k=0;k<numberofRules;k++)
			{
				aCombination[k] = counter[k];
			}
			counter[0]+=1;
			for(i=0;i<numberofRules;i++)
			{
				if (counter[i]>maxRuleApply[i])
				{
					if (i==(numberofRules-1))
					{
						breakCondition=true;
						break;
					}
					else
					{
						counter[i+1]+=1;
						counter[i]=0;
					}
				}
			}
			if (checkCombination(aCombination))
			{
				allPossibilities.add(aCombination);
			}
		}
		getMulisets(allPossibilities,level,uuid);
		return allPossibilities.size();
	}
	
	void getMulisets(ArrayList<long []> ruleSeuence,String level,String parentID) 
	{
		ArrayList<String> children= new ArrayList<String>();
		int i,rule;
		for(long[] aSequence: ruleSeuence )
		{
			rule=0;
			long[] aMuliset= new long[alphabetSize];
			for(i=0;i<alphabetSize;i++)
			{
				aMuliset[i]=0;
			}
			for(long [][] aRule: membraneRules )
			{
				for(i=0;i<alphabetSize;i++)
				{
					aMuliset[i]+=aRule[1][i]*aSequence[rule];
				}
			rule++;
			}
			
			ByteArrayOutputStream bo = null;
			try 
			{
				bo = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bo);
	            out.writeObject(aMuliset);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		
			List<String> majorComponents = Arrays.asList(level,membrane);
			String uuid = UUID.randomUUID().toString();
			List<String> minorComponents = Arrays.asList(uuid);
			db.store(majorComponents, minorComponents, bo.toByteArray());
			children.add(uuid);
		}
	ByteArrayOutputStream bo2 = null;
	try 
	{
		bo2 = new ByteArrayOutputStream();
		ObjectOutputStream out2 = new ObjectOutputStream(bo2);
        out2.writeObject(children);
	} 
	catch (IOException e) 
	{
		e.printStackTrace();
	}
	db.store(parentID, null, bo2.toByteArray());
	}
}
