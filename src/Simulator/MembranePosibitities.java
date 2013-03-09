package Simulator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

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
	public void readFields(DataInput in) throws IOException 
	{
		int newLength = WritableUtils.readVInt(in);
		byte[] membrane = new byte[newLength];
		in.readFully(membrane, 0, newLength);
		membraneLength = newLength;
		
		possibilities = (IntWritable[][]) new Writable[in.readInt()][];          
		for (int i = 0; i < possibilities.length; i++) 
		{
			possibilities[i] = (IntWritable[]) new Writable[in.readInt()];
		}
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

	@Override
	public void write(DataOutput out) throws IOException 
	{
		WritableUtils.writeVInt(out, membraneLength);
		out.write(membrane, 0, membraneLength);
		
		out.writeInt(possibilities.length);                 
		
		for (int i = 0; i < possibilities.length; i++) 
		{
			out.writeInt(possibilities[i].length);
		}
		for (int i = 0; i < possibilities.length; i++) 
		{
			for (int j = 0; j < possibilities[i].length; j++) 
			{
				possibilities[i][j].write(out);
			}
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
	

}
