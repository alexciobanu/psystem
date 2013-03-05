package DerivationTreeGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Interfaces.AbstractDatabase;
import Interfaces.MultiMembraneMultiset;


public class BrutForce implements ChildrenCalculator 
{
	ArrayList<MultiMembraneMultiset> rules;
	int[] currentMultiset;
	int alphabetSize;
	String membrane;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<int[]> findAllChildren(int[] multiset, String[] membranes, AbstractDatabase db) 
	{
		if (membranes.length>1)
		{
			System.out.println("this function does not support multiple membranes");
			return null;
		}
		membrane= membranes[0];
		rules = (ArrayList<MultiMembraneMultiset>)  db.retriveMembraneElement(membrane, "left");
		currentMultiset = multiset;
		alphabetSize = currentMultiset.length;
		int numberofRules = rules.size();
		ArrayList<int []> allPossibilities = new ArrayList<int []>();
		
		//get the maximum number of times each rule can be applied
		int[] maxRuleApply = new int[numberofRules];
		int i=0;
		int j;
		for(MultiMembraneMultiset aRule : rules)
		{
			int[] ruleConsumption = aRule.getMulisetForMembrane(membrane);
			int maxApply=Integer.MAX_VALUE;
			int apply = 0;
			for(j=0;j<alphabetSize;j++)
			{
				try{ apply=currentMultiset[j]/ruleConsumption[j]; } catch(Exception e) {apply=Integer.MAX_VALUE;}
				if (apply<maxApply) maxApply=apply;
			}
			maxRuleApply[i]=maxApply;
			i++;
		}
		
		int counter[]= new int[numberofRules]; //this is will a counter of the current combination being tried
		boolean breakCondition=false;
		int k;
		
		//go through all possible combinations and see which are maximal
		while(!breakCondition)
		{	
			int[] aCombination = new int[numberofRules];
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
		return allPossibilities;
	}
	
	private boolean checkCombination(int[] aCombination)
	{
		int objectsUsed []= new int[alphabetSize];
		int i,j;
		for(i=0;i<alphabetSize;i++)
		{
			objectsUsed[i]=0;
		}
		//calculate usage
		i=0;
		for(MultiMembraneMultiset aRule : rules)
		{
			int[] ruleConsumption = aRule.getMulisetForMembrane(membrane);
			for(j=0;j<alphabetSize;j++)
			{
				objectsUsed[j]+=ruleConsumption[j]*aCombination[i];
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

}
