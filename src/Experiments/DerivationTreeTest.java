package Experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import AdminTools.PsystemTools;
import DerivationTreeGenerator.ApplyAllRules;
import DerivationTreeGenerator.BrutForce;
import DerivationTreeGenerator.ChildrenCalculator;
import Interfaces.AbstractDatabase;
import Interfaces.MultiMembraneMultiset;
import Interfaces.NodeData;
import Interfaces.OracleNoSQLDatabase;

public class DerivationTreeTest 
{
	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		int level = 0;
		String membranes = "1"; 
		AbstractDatabase db = new OracleNoSQLDatabase("kvstore","localhost:5000");

		PsystemTools.grabPsystem("/home/a/Workspace/p2.pli");	
		List<String> IDS = db.retriveLevelIDs(level,membranes);
		NodeData aNode = db.RetrieveNode(IDS.get(0),level,membranes);
		int[] aMultiset = aNode.multiset;
		
		PsystemTools.printPsystem();
		
		ChildrenCalculator calc = new BrutForce();
		List<int[]> possiblilities = calc.findAllChildren(aMultiset, membranes, db);
        
		for(int[] aPoss: possiblilities)
		{
			System.out.println( Arrays.toString( aPoss ) );
		}
		System.out.println( "---------------------" );
		ArrayList<MultiMembraneMultiset> configurations = ApplyAllRules.getMulisets(possiblilities,membranes, db);
        
		for(MultiMembraneMultiset aConfig :  configurations)
		{
			int[] results = aConfig.getMulisetForMembrane(membranes);
			System.out.println( Arrays.toString( results ) );
		}
	}
}
