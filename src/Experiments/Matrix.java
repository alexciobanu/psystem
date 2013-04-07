package Experiments;

import java.util.*;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

//import java.lang.Math;
import org.apache.commons.math.fraction.Fraction;
import org.apache.commons.math.fraction.FractionConversionException;
 
class Matrix 
{
	LinkedList<LinkedList<Fraction>> matrix;
	LinkedList<String> constant;
	int numRows;
	int numCols;	
 
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
 
	Matrix(double [][] m) 
	{
		numRows = m.length;	
		numCols = m[0].length;
 
		matrix = new LinkedList<LinkedList<Fraction>>();
		constant = new LinkedList<String>();
 
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
 
	public void Interchange(Coordinate a, Coordinate b) 
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
 
	public void Scale(Coordinate x, Fraction d) {
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
 
	public void MultiplyAndAdd(Coordinate to, Coordinate from, Fraction scalar) {
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
 
	public void RREF() {
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
 
	public boolean isColumnZeroes(Coordinate a) {
		for (int i = 0; i < numRows; i++) {
			if (matrix.get(i).get(a.col).doubleValue() != 0.0) {
				return false;
			}
		}
 
		return true;
	}
 
	public boolean isRowZeroes(Coordinate a) {
		for (int i = 0; i < numCols; i++) {
			if (matrix.get(a.row).get(i).doubleValue() != 0.0) {
				return false;
			}
		}
		return true;
	}
 
	public Coordinate findPivot(Coordinate a) {
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
 
	public Fraction getCoordinate(Coordinate a) {
		return matrix.get(a.row).get(a.col);
	}
 
	public String toString() {
		return matrix.toString().replace("], ", "]\n");
	}
	
	public void get_solution_matrix() throws ScriptException
	{	
		int[] results = {4,3,5};
		int rowCounter=0;
		for( LinkedList<Fraction> row : matrix)
		{
			int k=0;
			for (int j = numRows; j < numCols; j++) 
			{
				Fraction value = row.get(j);
				if (value.floatValue() != 0)
				{
					System.out.print(row.get(j)+"t"+k+" ");
				}
				k++;
			}
			
		    ScriptEngineManager mgr = new ScriptEngineManager();
		    ScriptEngine engine = mgr.getEngineByName("JavaScript");
		    
		    String newString = (constant.get(rowCounter));
		    
		    for (int i=0;i<results.length;i++)
		    {
		    	
		    	newString = newString.replace( String.valueOf((char)(i+65)) , Integer.toString( results[i] ) );
		    }
			System.out.println(engine.eval(newString));
			
			rowCounter++;
		}
	}
 
	public static void main (String[] args) throws ScriptException 
	{
    	double[][] matrix_1 = {
    	{1, 1, 1, 0, 0},
		{1, 0, 0, 1, 0},
		{0, 1, 0, 0, 1}
		};   	
		Matrix x = new Matrix(matrix_1);

		x.RREF();
		//System.out.println("after\n" + x.toString() + "\n");
		x.get_solution_matrix();
		
	}	
}
