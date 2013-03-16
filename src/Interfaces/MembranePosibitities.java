package Interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

public class MembranePosibitities implements Writable
{
	private IntWritable[][] possibilities;
	private byte[] membrane;
	private int membraneLength;
	
	public MembranePosibitities()
	{
		
	}
	
	public MembranePosibitities(IntWritable[][] possibilities, String membrane )
	{
		this.setPossibilities(possibilities);
		this.membrane = membrane.getBytes(); 
		this.membraneLength = membrane.length();
	}
		
	@Override
	public void readFields(DataInput in) 
	{
		int newLength;
		try {
			newLength = in.readInt();
	
			membrane = new byte[newLength];
			in.readFully(membrane, 0, newLength);
			membraneLength = newLength;
			int length= in.readInt();
			int width= in.readInt();
			possibilities = new IntWritable[length][width];          
			// construct values
			for (int i = 0; i < possibilities.length; i++) 
			{
				for (int j = 0; j < possibilities[i].length; j++) 
				{
					IntWritable value = new IntWritable();  
					value.readFields(in);                       
					possibilities[i][j] = (IntWritable) value;         
				}
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void write(DataOutput out) 
	{
		
		try 
		{
			out.writeInt(membraneLength);

			out.write(membrane, 0, membraneLength);
			
			out.writeInt(possibilities.length);   
			out.writeInt(possibilities[0].length);

			for (int i = 0; i < possibilities.length; i++) 
			{
				for (int j = 0; j < possibilities[i].length; j++) 
				{
					possibilities[i][j].write(out);
				}
			}	
		
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public IntWritable[][] getPossibilities() 
	{
		return possibilities;
	}

	public void setPossibilities(IntWritable[][] possibilities) 
	{
		this.possibilities = possibilities;
	}
	
	public void setMembrane(String membrane) 
	{
		this.membrane = membrane.getBytes(); 
		this.membraneLength = membrane.length();
	}
	
	public String getMembrane() 
	{
		try 
		{
			return new String(membrane, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String toString()
	{
		String output= new String("");
		output+="Membrane:";
		try {
			output+=new String(membrane, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output+="\n";
		for(int i=0;i<possibilities.length;i++)
		{
			for(int j=0;j<possibilities[i].length;j++)
			{
				output+=possibilities[i][j].get(); 
				output+= " ";
			}
			output+="\n";
		}
		output+="\n";
		return output;
	}

}
