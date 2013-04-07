package AdminTools;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.commons.math.fraction.Fraction;
import org.apache.commons.math.fraction.FractionConversionException;

import Interfaces.AbstractDatabase;
import Interfaces.MultiMembraneMultiset;


public class EquationSolverLoader 
{
	LinkedList<LinkedList<Fraction>> matrix;
	ArrayList<String> constant;
	int numRows;
	int numCols;
	AbstractDatabase db;
 
	static class Coordinate 
	{
		int row;
		int col;
 
		Coordinate(int r, int c) 
		{
			row = r;
			col = c;
		}
 
		public String toString() 
		{
			return "(" + row + ", " + col + ")";
		}
	}
 
	private void initMatrix(double [][] m) 
	{
		numRows = m.length;	
		numCols = m[0].length;
 
		matrix = new LinkedList<LinkedList<Fraction>>();
		constant = new ArrayList<String>();
 
		for (int i = 0; i < numRows; i++) 
		{
			matrix.add(new LinkedList<Fraction>());
			String aString = new String();
			aString += (char) (i+65);
			constant.add( aString );
			
			for (int j = 0; j < numCols; j++) 
			{
				try 
				{
					matrix.get(i).add(new Fraction(m[i][j]));
				} 
				catch (FractionConversionException e) 
				{
					System.err.println("Fraction could not be converted from double by apache commons . . .");
				}
			}
		}
	}
 
	private void Interchange(Coordinate a, Coordinate b) 
	{
		LinkedList<Fraction> temp = matrix.get(a.row);
		String tempString = constant.get(a.row);  
		matrix.set(a.row, matrix.get(b.row));		
		matrix.set(b.row, temp);
		
		constant.set(a.row, constant.get(b.row));
		constant.set(b.row, tempString);
 
		int t = a.row;
		a.row = b.row;
		b.row = t;
	} 
 
	private void Scale(Coordinate x, Fraction d) {
		LinkedList<Fraction> row = matrix.get(x.row);
		String a = constant.get(x.row);
		String b = constant.get(x.row);
		b = "(" + a  + ")" + "*" + d;
		constant.set(x.row, b);
		for (int i = 0; i < numCols; i++) 
		{
			row.set(i, row.get(i).multiply(d));
		}
	}
 
	private void MultiplyAndAdd(Coordinate to, Coordinate from, Fraction scalar) {
		LinkedList<Fraction> row = matrix.get(to.row);
		LinkedList<Fraction> rowMultiplied = matrix.get(from.row);
		
		String theRow = constant.get(to.row);
		String theMultiplied= constant.get(from.row);
		theRow+="+ (" + theMultiplied + ")*" + scalar;
		constant.set(to.row, theRow);
 
		for (int i = 0; i < numCols; i++) {
			row.set(i, row.get(i).add((rowMultiplied.get(i).multiply(scalar))));
		}
	}
 
	private void RREF() {
		Coordinate pivot = new Coordinate(0,0);
 
		int submatrix = 0;
		for (int x = 0; x < numCols; x++) {
			pivot = new Coordinate(pivot.row, x);
			//Step 1
				//Begin with the leftmost nonzero column. This is a pivot column. The pivot position is at the top.
				for (int i = x; i < numCols; i++) {
					if (isColumnZeroes(pivot) == false) {
						break;	
					} else {
						pivot.col = i;
					}
				}
			//Step 2
				//Select a nonzero entry in the pivot column with the highest absolute value as a pivot. 
				pivot = findPivot(pivot);
 
				if (getCoordinate(pivot).doubleValue() == 0.0) {
					pivot.row++;
					continue;
				}
 
				//If necessary, interchange rows to move this entry into the pivot position.
				//move this row to the top of the submatrix
				if (pivot.row != submatrix) {
					Interchange(new Coordinate(submatrix, pivot.col), pivot);
				}
 
				//Force pivot to be 1
				if (getCoordinate(pivot).doubleValue() != 1) 
				{
					//System.out.println(getCoordinate(pivot));
					//System.out.println(pivot);
					//System.out.println(matrix);
					
					Fraction scalar = getCoordinate(pivot).reciprocal();
					Scale(pivot, scalar);
				}
			//Step 3
				//Use row replacement operations to create zeroes in all positions below the pivot.
				//belowPivot = belowPivot + (Pivot * -belowPivot)
				for (int i = pivot.row; i < numRows; i++) {
					if (i == pivot.row) {
						continue;
					}
					Coordinate belowPivot = new Coordinate(i, pivot.col);
					Fraction complement = (getCoordinate(belowPivot).negate().divide(getCoordinate(pivot)));
					MultiplyAndAdd(belowPivot, pivot, complement);
				}
			//Step 5
				//Beginning with the rightmost pivot and working upward and to the left, create zeroes above each pivot.
				//If a pivot is not 1, make it 1 by a scaling operation.
					//Use row replacement operations to create zeroes in all positions above the pivot
				for (int i = pivot.row; i >= 0; i--) {
					if (i == pivot.row) {
						if (getCoordinate(pivot).doubleValue() != 1.0) {
							Scale(pivot, getCoordinate(pivot).reciprocal());	
						}
						continue;
					}
					if (i == pivot.row) {
						continue;
					}
 
					Coordinate abovePivot = new Coordinate(i, pivot.col);
					Fraction complement = (getCoordinate(abovePivot).negate().divide(getCoordinate(pivot)));
					MultiplyAndAdd(abovePivot, pivot, complement);
				}
			//Step 4
				//Ignore the row containing the pivot position and cover all rows, if any, above it.
				//Apply steps 1-3 to the remaining submatrix. Repeat until there are no more nonzero entries.
				if ((pivot.row + 1) >= numRows || isRowZeroes(new Coordinate(pivot.row+1, pivot.col))) {
					break;
				}
 
				submatrix++;
				pivot.row++;
		}
	}
 
	private boolean isColumnZeroes(Coordinate a) {
		for (int i = 0; i < numRows; i++) {
			if (matrix.get(i).get(a.col).doubleValue() != 0.0) {
				return false;
			}
		}
 
		return true;
	}
 
	private boolean isRowZeroes(Coordinate a) {
		for (int i = 0; i < numCols; i++) {
			if (matrix.get(a.row).get(i).doubleValue() != 0.0) {
				return false;
			}
		}
		return true;
	}
 
	private Coordinate findPivot(Coordinate a) {
		int first_row = a.row;
		Coordinate pivot = new Coordinate(a.row, a.col);
		Coordinate current = new Coordinate(a.row, a.col);	
 
		for (int i = a.row; i < (numRows - first_row); i++) {
			current.row = i;
			if (getCoordinate(current).doubleValue() == 1.0) {
				Interchange(current, a);
			}
		}
 
		current.row = a.row;
		for (int i = current.row; i < (numRows - first_row); i++) {
			current.row = i;
			if (getCoordinate(current).doubleValue() != 0) {
				pivot.row = i;
				break;
			}
		}
		return pivot;	
	}	
 
	private Fraction getCoordinate(Coordinate a) {
		return matrix.get(a.row).get(a.col);
	}
 
	public String toString() {
		return matrix.toString().replace("], ", "]\n");
	}
	
	public int[][] calculateSolutionMatrix()
	{	
		int[][] solutionsMatrix = new int[numCols][numCols-numRows];
		int i=0;
		for( LinkedList<Fraction> row : matrix)
		{
			int j=0;
			for (int k = numRows; k< numCols; k++) 
			{
				solutionsMatrix[i][j]=row.get(k).intValue()*-1;
				//System.out.print(row.get(k)+"t"+k+" ");
				j++;
			}
		i++;
		}
		int j=0;
		while (i<numCols)
		{
			solutionsMatrix[i][j]=1;
			i++;
			j++;
		}

		return solutionsMatrix;
	}

	public void findSolutionMatrix(AbstractDatabase theDB) 
	{
		db = theDB;
		String[] membranes = db.retriveMembraneList();
		
		for(String aMembrane: membranes)
		{
			@SuppressWarnings("unchecked")
			ArrayList<MultiMembraneMultiset> rules  = (ArrayList<MultiMembraneMultiset>) db.retriveMembraneElement(aMembrane, "left");
			double[][] equationMatrix = new double[(rules.get(0)).getMulisetForMembrane(aMembrane).length][rules.size()];
			int i=0;
			for(MultiMembraneMultiset aRule : rules)
			{
				int[] ruleVector = aRule.getMulisetForMembrane(aMembrane) ;
				for(int j=0;j<ruleVector.length;j++)
				{
					equationMatrix[j][i]=ruleVector[j];
				}
				i++;
			}
			initMatrix(equationMatrix);
			RREF();
			int[][] sol = calculateSolutionMatrix();
			db.StoreMembraneSolutionMatrix(aMembrane, sol, constant );
		}
	}
}
