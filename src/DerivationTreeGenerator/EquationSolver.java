package DerivationTreeGenerator;

import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import Interfaces.AbstractDatabase;

public class EquationSolver implements ChildrenCalculator 
{
	
	float[][] solutionsMatrix;
	float[] max; 
	float[] min; 
	ArrayList<float []> retestRules = new ArrayList<float []>(); 
	ArrayList<int []> allCombinations = new ArrayList<int []>(); 

	@Override
	public List<int[]> findAllChildren(int[] multiset, String membrane, AbstractDatabase db) 
	{
		float[][] initialSolutionMatrix = db.RetriveMembraneSolutionMatrix(membrane);
		List<String> constants = db.RetriveMembraneSolutionConstants(membrane);
		GenerateaugmentedMatrix(initialSolutionMatrix, constants, multiset);
		//printSolutionMatrix();
		max = new float[solutionsMatrix[0].length-1];
		min = new float[solutionsMatrix[0].length-1];
		findMaxMin();
		goThroughAllCombinations();
		return allCombinations;
	}
	
	private void GenerateaugmentedMatrix(float[][] initialSolutionMatrix, List<String> constants, int[] multiset)
	{
		solutionsMatrix = new float[initialSolutionMatrix.length][initialSolutionMatrix[0].length+1];
		
		for(int i=0;i<initialSolutionMatrix.length;i++)
		{
			for(int j=0;j<initialSolutionMatrix[i].length;j++)
			{
				solutionsMatrix[i][j] = initialSolutionMatrix[i][j];
			}
		}

	    ScriptEngineManager mgr = new ScriptEngineManager();
	    ScriptEngine engine = mgr.getEngineByName("JavaScript");
	    int rowCounter=0; 
	    int column = initialSolutionMatrix[0].length;
	    for(String aConstant: constants)
	    {
	    	while (partOfIdentity( solutionsMatrix[rowCounter]) )
	    	{
	    		rowCounter++;
	    	}
		    for (int i=0;i<multiset.length;i++)
		    {
		    	aConstant = aConstant.replace( String.valueOf((char)(i+65)) , Integer.toString( multiset[i] ) );
		    }
			try 
			{
				String value = engine.eval(aConstant).toString();
				//System.out.println(rowCounter +  "" + value);
				Double buff = Double.parseDouble(value);
				solutionsMatrix[rowCounter][column]= buff.floatValue();
			} 
			catch (ScriptException e) 
			{
				e.printStackTrace();
			}
			rowCounter++;
	    }
	}
	
	
	private boolean partOfIdentity(float [] array)
	{
		int numberOfOnes=0;
		for(int i=0;i<array.length;i++)
		{
			if (array[i]==1)
			{
				numberOfOnes++;
			}
			else if (array[i]!=0)
			{
				return false;
			}
			if (numberOfOnes>1)
			{
				return false;
			}		
		}
		if (numberOfOnes==1)
			return true;
		else
			return false;
	}
	
	private void findFullCombination(float [] combination)
	{
		int[] result= new int[solutionsMatrix.length];
		for(int i=0;i<solutionsMatrix.length;i++)
		{
			float buff=0;
			for(int j=0;j<solutionsMatrix[i].length;j++)
			{
				if (j<solutionsMatrix[i].length-1)
					buff+=solutionsMatrix[i][j]*combination[j];
				else
					buff+=solutionsMatrix[i][j];
			}
			if ((int) buff == buff)
				result[i]= (int) buff;
			else
				return;
		}
		allCombinations.add(result);
		//System.out.println(Arrays.toString(result));
	}

	private void findMaxMin()
	{
		for(int i=0;i<max.length;i++)
		{
			max[i]=Integer.MIN_VALUE;
			min[i]=Integer.MAX_VALUE;
		}
		
		int numberOfOnes;
		int j;
		int index;
		//look for rows of the matrix where there is a single 1. 
		//That means we can extract directly a max or a min for one of the free variables 
		for(int i=0;i<solutionsMatrix.length;i++)
		{
			numberOfOnes=0;
			j=0;
			index=-1;
			while(numberOfOnes<2 && j<solutionsMatrix[i].length-1)
			{
				if( solutionsMatrix[i][j]!=0 ) 
				{
					numberOfOnes++;
					index=j;
				}
			j++;
			}
			if(numberOfOnes==1)
			{
				float value =  solutionsMatrix[i][solutionsMatrix[i].length-1] / solutionsMatrix[i][index];
				if (solutionsMatrix[i][index]>0)
				{
					if (min[index]>value)
					{
						min[index]=value;
					}
				}
				else 
				{
					value*=-1;
					if (max[index]<value)
					{
						max[index]=value;
					}
				}
			}
			else
			{
				retestRules.add(solutionsMatrix[i]);
			}
		}
		for(int i=0;i<max.length;i++)
		{
			if (max[i]==Integer.MIN_VALUE)
			{
				max[i]=1;
			}
			
		}
		
	}
	
	private void goThroughAllCombinations()
	{
		//create a counter to go through every possible combination between max and min for each free variable
		float counter[]= new float[max.length];
		
		for(int i=0;i<counter.length;i++)
		{
			counter[i]=min[i];
		}
		
		int k;
		boolean breakCondition = false;
		do 
		{	
			float[] aCombination = new float[max.length];
			for(k=0;k<max.length;k++)
			{
				aCombination[k] = counter[k];
			}
			//check a particular combination
			boolean combinationTest=true;
			for (float [] aRule : retestRules)
			{
				float total= 0;
				for(int i=0;i<aRule.length;i++)
				{
					if (i<(aRule.length-1))
						total+= aCombination[i]*aRule[i];
					else
						total+= aRule[i];
				}
				if (total<0)
				{
					combinationTest=false;
					break;
				}
			}
			if (combinationTest)
				findFullCombination(counter);
			
			//increment the counter by 1 and make appropriate adjustments to the rest of the digits
			counter[0]+=1;
			for(k=0;k<max.length;k++)
			{
				if (counter[k]>max[k])
				{
					if (k==(max.length-1))
					{
						breakCondition=true;
						break;
					}
					else
					{
						counter[k+1]+=1;
						counter[k]=0;
					}
				}
			}
		} while(!breakCondition);
	}
	
	public void printSolutionMatrix()
	{
		for(int i=0;i<solutionsMatrix.length;i++)
		{
			System.out.print("[");
			for(int j=0;j<solutionsMatrix[i].length;j++)
			{
				System.out.print(String.format("%3f", solutionsMatrix[i][j]));
			}
			System.out.println("]");
		}	
	}
}
