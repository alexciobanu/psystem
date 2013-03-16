package Interfaces;

import java.io.Serializable;
import java.util.ArrayList;

public class MultiMembraneMultiset implements Serializable
{
	private static final long serialVersionUID = 2721814963913131643L;
	private ArrayList<String> Membranes;
	private ArrayList<int []> Multisets;
	
	public MultiMembraneMultiset ()
	{
		Membranes = new ArrayList<String>();
		Multisets = new	ArrayList<int []>();
	}
	
	public MultiMembraneMultiset (ArrayList<String> membranes, ArrayList<int []> multisets)
	{
		Membranes = membranes;
		Multisets = multisets;
	}
	
	public void add(String membrane, int [] multiset )
	{
		if (Membranes.contains(membrane))
		{
			int index = Membranes.indexOf(membrane);
			int[] data = Multisets.get(index);
			if ( data.length != multiset.length )
			{
				System.err.println("Error THE SIZE OF THE INCOMMING AND CURRENT multisets are not the same");
			}
			for(int i=0;i<multiset.length;i++)
			{
				data[i]+=multiset[i];
			}
			//Multisets.set(index, data);
		}
		else
		{
			Membranes.add(membrane);
			Multisets.add(multiset);
		}
	}
	
	public int getNumberOfMembranes()
	{
		return Membranes.size();
	}
	
	public ArrayList<String> getRules()
	{
		return Membranes;
	}
	
	public int [] getMulisetForMembrane(String membrane)
	{
		
		int index = Membranes.indexOf(membrane);
		return Multisets.get(index);
	}
	
	public ArrayList<String> getMembranes()
	{
		return Membranes;
	}
	
	public ArrayList<int []> getMultisets()
	{
		return Multisets;
	}

}
