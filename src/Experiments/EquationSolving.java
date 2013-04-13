package Experiments;

import java.util.Arrays;
import java.util.List;


import AdminTools.EquationSolverLoader;
import DerivationTreeGenerator.BrutForce;
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
		int[] multiset={1,1,1,1,1};
		EquationSolver  calc = new EquationSolver();
		ChildrenCalculator calc2 = new BrutForce();
		List<int[]> possibilities = calc.findAllChildren(multiset, membranes[0], db);
		List<int[]> possibilities2 = calc2.findAllChildren(multiset, membranes[0], db);
		calc.printSolutionMatrix();
		System.out.println("-------------"+ membranes[0] +"--------------------");
		for(int[] aPossibility: possibilities)
		{
			System.out.println(Arrays.toString(aPossibility));
		}
		System.out.println("---------------------------------");
		for(int[] aPossibility: possibilities2)
		{
			System.out.println(Arrays.toString(aPossibility));
		}
	}
	
}
