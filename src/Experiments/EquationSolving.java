package Experiments;

import java.util.Arrays;
import java.util.List;


import AdminTools.EquationSolverLoader;
import DerivationTreeGenerator.ChildrenCalculator;
import DerivationTreeGenerator.EquationSolver;
import Interfaces.AbstractDatabase;
import Interfaces.OracleNoSQLDatabase;

public class EquationSolving 
{
	
	public static void main(String[] args) 
	{

		AbstractDatabase db = new OracleNoSQLDatabase("PsystemStore","hadoop1:5000");
		EquationSolverLoader bla = new EquationSolverLoader();
		bla.findSolutionMatrix(db);
		String[] membranes = db.retriveMembraneList();
		int[] multiset={4,3,5};
		ChildrenCalculator  eqslvr = new EquationSolver();
		List<int[]> possibilities = eqslvr.findAllChildren(multiset, membranes[0], db);
		
		for(int[] aPossibility: possibilities)
		{
			System.out.println(Arrays.toString(aPossibility));
		}
	}
	
}
