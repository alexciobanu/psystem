package DerivationTreeGenerator;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.IntWritable;

import Interfaces.AbstractDatabase;
import Interfaces.MultiMembraneMultiset;

public class ApplyAllRules 
{
	@SuppressWarnings("unchecked")
	
	public static ArrayList<MultiMembraneMultiset> getMulisets(List<int[]> ruleSeuence,String membrane,AbstractDatabase db) 
	{
		ArrayList<MultiMembraneMultiset> rules;
		rules = (ArrayList<MultiMembraneMultiset>)  db.retriveMembraneElement(membrane, "right");
		
		ArrayList<MultiMembraneMultiset> finalConfigurations= new ArrayList<MultiMembraneMultiset>();
		int i;
		for(int[] aSequence: ruleSeuence )
		{
			MultiMembraneMultiset producedConfig = new MultiMembraneMultiset();
			for(i=0;i<aSequence.length;i++)
			{
				if (aSequence[i]!=0)
				{
					MultiMembraneMultiset myRule = rules.get(i);
					for(String myMembrane : myRule.getMembranes())
					{	
						int[] multiset = (myRule.getMulisetForMembrane(myMembrane)).clone();
						for(int j=0;j<multiset.length;j++)
						{
							multiset[j]*=aSequence[i];
						}
						producedConfig.add(myMembrane, multiset);	
					}
				}
			}
			finalConfigurations.add(producedConfig);
		}
		return finalConfigurations;
	}
	
	@SuppressWarnings("unchecked")
	public static void addMulisetsToConfiguration(MultiMembraneMultiset aConfig, IntWritable[] aSequence,String membrane,AbstractDatabase db) 
	{
		ArrayList<MultiMembraneMultiset> rules;
		rules = (ArrayList<MultiMembraneMultiset>)  db.retriveMembraneElement(membrane, "right");
		
		//ArrayList<MultiMembraneMultiset> finalConfigurations= new ArrayList<MultiMembraneMultiset>();
		//MultiMembraneMultiset producedConfig = new MultiMembraneMultiset();
		for(int i=0;i<aSequence.length;i++)
		{
			int ithValue = aSequence[i].get();
			if (ithValue!=0)
			{
				MultiMembraneMultiset myRule = rules.get(i);
				for(String myMembrane : myRule.getMembranes())
				{	
					int[] multiset = (myRule.getMulisetForMembrane(myMembrane)).clone();
					for(int j=0;j<multiset.length;j++)
					{
						multiset[j]*=ithValue;
					}
					aConfig.add(myMembrane, multiset);	
				}
			}
		}
	}
}
