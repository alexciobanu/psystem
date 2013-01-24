package InputFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oracle.kv.Key;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

public class NoSQLSplit extends InputSplit implements Writable
{
	private ArrayList<Key> keyArray;
	
	public NoSQLSplit()
	{
		keyArray = new ArrayList<Key>();
	}
	
	public NoSQLSplit(ArrayList<Key> keyArray)
	{
		this.keyArray=keyArray;
	}
	
	public ArrayList<Key> getKeys()
	{
		return keyArray;
	}
	
	@Override
	public long getLength() throws IOException, InterruptedException 
	{
		return keyArray.size();
	}

	@Override
	public String[] getLocations() throws IOException, InterruptedException 
	{
		String [] hosts = {"machine1"};
		return hosts;
	}

	@Override
	public void readFields(DataInput in) throws IOException 
	{
		int numKeys = in.readInt();

		for(int i=0;i<numKeys;i++)
		{
			String  rawString = in.readUTF();
			String[] components = rawString.split("\t");
			List<String> majorComponent = Arrays.asList( components[0].split(" ") ); 
			List<String> minorComponent = Arrays.asList( components[1].split(" ") ); 
			Key aKey = Key.createKey(majorComponent, minorComponent);
			keyArray.add(aKey);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException 
	{
		out.writeInt(keyArray.size());
		for(Key aKey : keyArray)
		{
			String myKey="";
			for(String majorPath: aKey.getMajorPath())
			{
				myKey+=majorPath+" ";
			}
			myKey+="\t";
			for(String minorPath: aKey.getMinorPath())
			{
				myKey+=minorPath+" ";
			}
			out.writeUTF(myKey);
		}
	}
}
