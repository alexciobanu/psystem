package AdminTools;

import java.util.List;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.gcn.plinguacore.parser.AbstractParserFactory;
import org.gcn.plinguacore.parser.input.InputParser;
import org.gcn.plinguacore.parser.input.InputParserFactory;
import org.gcn.plinguacore.util.MultiSet;
import org.gcn.plinguacore.util.psystem.Psystem;
import org.gcn.plinguacore.util.psystem.cellLike.membrane.CellLikeMembrane;
import org.gcn.plinguacore.util.psystem.cellLike.membrane.CellLikeNoSkinMembrane;
import org.gcn.plinguacore.util.psystem.cellLike.membrane.CellLikeSkinMembrane;
import org.gcn.plinguacore.util.psystem.membrane.Membrane;
import org.gcn.plinguacore.util.psystem.rule.IRule;
import org.gcn.plinguacore.util.psystem.rule.InnerRuleMembrane;

import Interfaces.AbstractDatabase;
import Interfaces.MultiMembraneMultiset;
import Interfaces.NodeData;

public class PsystemTools 
{	
	/*public static void main(String[] args)
	{
		OracleNoSQLDatabase db = new OracleNoSQLDatabase("PsystemStore","hadoop1:5000");
		printLevel(1, db);
	}*/
	
	@SuppressWarnings("unchecked")
	public static void printPsystem(AbstractDatabase db)
	{
		String[] alphabet = db.retriveAlphbaet();
		System.out.print("Alphabet = ");
		for(int i=0;i<alphabet.length;i++)
		{
			System.out.print( alphabet[i] + "," );
		}
		System.out.println();
		
		String[] membranes = db.retriveMembraneList();
		for(int i=0;i<membranes.length;i++)
		{
			String parent = (String) db.retriveMembraneElement(membranes[i], "ParentMembrane");
			String[] children = (String[]) db.retriveMembraneElement(membranes[i], "ChildrenMembranes");
			System.out.println("Membrane: " + membranes[i] + " Parent: " + parent + " Children: " + Arrays.toString(children) );
		}
		//Print Rules users
		for(int i=0;i<membranes.length;i++)
		{
			ArrayList<MultiMembraneMultiset > RulesLeft = (ArrayList<MultiMembraneMultiset > ) db.retriveMembraneElement(membranes[i], "left");
			ArrayList<MultiMembraneMultiset > RulesRight = (ArrayList<MultiMembraneMultiset > ) db.retriveMembraneElement(membranes[i], "right");
			System.out.println("Membrane: " + membranes[i] + " Uses: ");
			for (MultiMembraneMultiset aRule: RulesLeft)
			{
				for(String aMembrane: aRule.getMembranes())
				{
					System.out.println("\t Membrane " + aMembrane + Arrays.toString( aRule.getMulisetForMembrane(aMembrane) ));
				}
			}
			System.out.println("Membrane: " + membranes[i] + " Produces: ");
			for (MultiMembraneMultiset aRule: RulesRight)
			{
				for(String aMembrane: aRule.getMembranes())
				{
					System.out.println("\t Membrane " + aMembrane + Arrays.toString( aRule.getMulisetForMembrane(aMembrane) ));
				}
			}
			System.out.println();
		}

		//print the multisets of a level
		printLevel(0,db);
	}
	
	public static void printLevel(int level, AbstractDatabase db)
	{
		String[] membranes = db.retriveMembraneList();
		for (int i=0;i<membranes.length;i++)
		{
			List<String> ids = db.retriveLevelIDs(level,membranes[i]);
			System.out.println("Membrane " + membranes[i] + " :" );
			for (String anID : ids)
			{
				NodeData aNode = db.RetrieveNode(anID, level, membranes[i]);
				System.out.println("MultiSet:" + Arrays.toString(aNode.multiset) + " Rules " + Arrays.toString( aNode.rules ) );
			}
		}
	}
	
	
	private static void grabMembranes(Psystem ps, AbstractDatabase db)
	{
		String[] membranes = new String[ps.getMembraneStructure().getAllMembranes().size()];
		int i=0;
		for (Membrane aMembrane : ps.getMembraneStructure().getAllMembranes() )
		{
			//if the membrane does NOT have parents
			membranes[i]= aMembrane.getLabel();
			String[] children;
			if (aMembrane instanceof CellLikeSkinMembrane)
			{
				CellLikeMembrane cellSkinMembrane = (CellLikeMembrane) aMembrane;
				children = new String[cellSkinMembrane.getChildMembranes().size()];
				int j=0;
				for (Membrane aChild : cellSkinMembrane.getChildMembranes())
				{
					children[j] = aChild.getLabel();
					j++;
				}
				db.storeMembraneElement(membranes[i], "ChildrenMembranes", children);
				db.storeMembraneElement(membranes[i], "ParentMembrane", "");
			}
			//if the membrane does have parents
			if (aMembrane instanceof CellLikeNoSkinMembrane)
			{
				CellLikeNoSkinMembrane cellMembrane = (CellLikeNoSkinMembrane) aMembrane;
				children = new String[cellMembrane.getChildMembranes().size()];
				int j=0;
				for (Membrane aChild : cellMembrane.getChildMembranes())
				{
					children[j] = aChild.getLabel();
					j++;
				}
				db.storeMembraneElement(membranes[i], "ChildrenMembranes", children);
				String parent = cellMembrane.getParentMembrane().getLabel();
				db.storeMembraneElement(membranes[i], "ParentMembrane", parent);
			}
			i++;
		}
		db.storeMembraneList(membranes);
	}
	
	public static void grabPsystem(String fileName, AbstractDatabase db)
	{
		try
		{
			FileInputStream stream = new FileInputStream(fileName);
			AbstractParserFactory pf = new InputParserFactory();
			InputParser parser = (InputParser) pf.createParser("P-Lingua");
			int i;
			Psystem ps = parser.parse(stream);
			
			//Alphabet
			Object[] buff = ps.getAlphabet().toArray();
			String[] alphabet = new String[buff.length];
			for(i=0;i<buff.length;i++)
			{
				 alphabet[i] = buff[i].toString();
			}
	        db.storeAlphbaet(alphabet);
			
	        grabMembranes(ps,db);
			
			//Initial Multiset
			int alphabetSize = alphabet.length;	
			
			String uuid = UUID.randomUUID().toString();
			for(Membrane aMembrane : ps.getMembraneStructure().getAllMembranes())
			{	
				MultiSet<String> initialMultiset= ps.getInitialMultiSets().get(aMembrane.getLabel());
				if (initialMultiset==null)
					break;
				int objects[] = new int[alphabetSize];
				for(i=0;i<alphabetSize;i++)
				{	
					objects[i]=(int) initialMultiset.count(alphabet[i].toString());
				}
				NodeData theNode = new NodeData();
				theNode.parent="";
				theNode.multiset=objects;
				theNode.rules=null;
				theNode.duplicate="";
				db.StoreNode(uuid, 0, aMembrane.getLabel(), theNode);
			}
			// Rules 
			List<String> ids = grabRawRules(db, ps, alphabet);
			storeSortedRules(ids, db);
			db.deleteAllTemp();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
	
	private static List<String> grabRawRules(AbstractDatabase db, Psystem ps, String[] alphabet)
	{
		//Rules raw
		int j;
		int alphabetSize = alphabet.length;
		ArrayList<String> ruleIDs = new ArrayList<String>();
		for (IRule currentRule : ps.getRules())
		{	
			int[] RulesUsingHere =null;
			int[] RulesUsingParent =null;
			int[][] RulesUsingChildren =null;
			int[] RulesProducingHere =null;
			int[] RulesProducingParent =null;
			int[][] RulesProducingChildren =null;
			String membrane = currentRule.getLeftHandRule().getOuterRuleMembrane().getLabel();
			String parent = (String) db.retriveMembraneElement(membrane, "ParentMembrane");
			String[] childMembranes = (String[]) db.retriveMembraneElement(membrane, "ChildrenMembranes");
			int innerMembranesSize = childMembranes.length;
			
			RulesUsingHere = new int[alphabetSize];
			RulesUsingParent = new int[alphabetSize];
			RulesProducingHere = new int[alphabetSize];
			RulesProducingParent = new int[alphabetSize]; 
			RulesUsingChildren = new int[innerMembranesSize][alphabetSize]; 
			RulesProducingChildren = new int[innerMembranesSize][alphabetSize];
			
			for(j=0;j<alphabetSize;j++)
			{
				//go through all the object in the current membrane left side
				MultiSet<String> leftCurrentMultiset = currentRule.getLeftHandRule().getOuterRuleMembrane().getMultiSet();
				RulesUsingHere[j]= (int) leftCurrentMultiset.count(alphabet[j].toString());
				//go through all the object in the current membrane right side
				MultiSet<String> rightCurrentMultiset = currentRule.getRightHandRule().getOuterRuleMembrane().getMultiSet();
				RulesProducingHere[j]= (int) rightCurrentMultiset.count(alphabet[j].toString());
				//go though all the objects in the parent membrane left side
				MultiSet<String> leftParentMultiset = currentRule.getLeftHandRule().getMultiSet();
				RulesUsingParent[j]= (int) leftParentMultiset.count(alphabet[j].toString());

				//go through all the objects in the parent membrane right side
				MultiSet<String> rightParentMultiset = currentRule.getRightHandRule().getMultiSet();
				RulesProducingParent[j]= (int) rightParentMultiset.count(alphabet[j].toString());
				//go through all the children membranes left hand side
				List<InnerRuleMembrane> innerMembranes = currentRule.getLeftHandRule().getOuterRuleMembrane().getInnerRuleMembranes();
				int i=0;
				for (InnerRuleMembrane aMembrane : innerMembranes )
				{
					MultiSet<String> aMultiSet = aMembrane.getMultiSet();
					RulesUsingChildren[i][j]= (int) aMultiSet.count(alphabet[j].toString());
				}
				//go through all the children right hand side
				i=0;
				innerMembranes = currentRule.getRightHandRule().getOuterRuleMembrane().getInnerRuleMembranes();

				for (InnerRuleMembrane aMembrane : innerMembranes )
				{
					MultiSet<String> aMultiSet = aMembrane.getMultiSet();
					RulesProducingChildren[i][j]= (int) aMultiSet.count(alphabet[j].toString());
				}

			}
			String ruleID = UUID.randomUUID().toString();

			if (!emptyArray(RulesUsingHere))
				db.storeTempElement(membrane,ruleID,membrane,true,RulesUsingHere);
			if (!emptyArray(RulesUsingParent))
				db.storeTempElement(membrane,ruleID,parent,true,RulesUsingParent);
			for (int i=0;i<childMembranes.length; i++)
			{
				if (!emptyArray(RulesUsingParent))
					db.storeTempElement(membrane,ruleID,childMembranes[i],true,RulesUsingChildren);
			}
			if (!emptyArray(RulesProducingHere))
				db.storeTempElement(membrane,ruleID,membrane,false,RulesProducingHere);
			if (!emptyArray(RulesProducingParent))
				db.storeTempElement(membrane,ruleID,parent,false,RulesProducingParent);
			for (int i=0;i<childMembranes.length; i++)
			{
				if (!emptyArray(RulesUsingParent))
					db.storeTempElement(membrane,ruleID,childMembranes[i],false,RulesProducingChildren);
			}	
			ruleIDs.add(ruleID);
		}
		return ruleIDs;
	}
	
	public static boolean emptyArray(int[] theArray)
	{
		for(int i=0;i<theArray.length;i++)
			if (theArray[i]!=0)
				return false;
		return true;
	}

	private static void storeSortedRules(List<String> ids, AbstractDatabase db)
	{
		String[] membranes = db.retriveMembraneList();
		int i=0;
		for (i=0;i<membranes.length;i++) 
		{
			ArrayList<MultiMembraneMultiset> aggregradedRulesRight = new ArrayList<MultiMembraneMultiset>();
			ArrayList<MultiMembraneMultiset> aggregradedRulesLeft = new ArrayList<MultiMembraneMultiset>();
			for(String aRuleID: ids)
			{
				MultiMembraneMultiset aRuleRight = db.retriveTempElement(membranes[i], aRuleID,true);
				MultiMembraneMultiset aRuleLeft = db.retriveTempElement(membranes[i], aRuleID,false );
				if ((aRuleLeft.getNumberOfMembranes()>0)||(aRuleRight.getNumberOfMembranes()>0))
				{
					aggregradedRulesLeft.add(aRuleLeft);
					aggregradedRulesRight.add(aRuleRight);
				}
			}
			db.storeMembraneElement(membranes[i], "left", aggregradedRulesLeft);
			db.storeMembraneElement(membranes[i], "right", aggregradedRulesRight);
		}
		
		
	}
}
