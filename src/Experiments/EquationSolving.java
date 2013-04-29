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
		int membraneNumber=2;
		AbstractDatabase db = new OracleNoSQLDatabase("kvstore","Desktop:5000");
		EquationSolverLoader bla = new EquationSolverLoader();
		bla.findSolutionMatrix(db);
		String[] membranes = db.retriveMembraneList();
		int[] multiset={1,1,1,1,1};
		ChildrenCalculator calc = new BrutForce();
		long startTime = System.currentTimeMillis();
		List<int[]> possibilities = calc.findAllChildren(multiset, membranes[membraneNumber], db);
		long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime) );
		
		System.out.println("-------------"+ membranes[membraneNumber] +"--------------------");
		for(int[] aPossibility: possibilities)
		{
			System.out.println(Arrays.toString(aPossibility));
		}
		
		EquationSolver  calc2 = new EquationSolver();
		startTime = System.currentTimeMillis();
		List<int[]> possibilities2 = calc2.findAllChildren(multiset, membranes[membraneNumber], db);
		endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime) );
		
		System.out.println("---------------------------------");
		for(int[] aPossibility: possibilities2)
		{
			System.out.println(Arrays.toString(aPossibility));
		}
	}
	
}
