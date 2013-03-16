package OutputFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;
import oracle.kv.Key;


public class KeyWritable implements WritableComparable<KeyWritable>
{
	Key key;
	
	public Key getKey() 
	{
		return key;
	}

	public void setKey(Key key) 
	{
		this.key = key;
	}

	public KeyWritable(Key key) 
	{
		super();
		this.key= key;
	}
	
	public KeyWritable() 
	{
		super();
	}

	@Override
	public void readFields(DataInput in) throws IOException 
	{
		int size = in.readInt();
		byte[] data = new byte[size];	
		in.readFully(data, 0, size);
		this.key = Key.fromByteArray(data);
	}

	@Override
	public void write(DataOutput out) throws IOException 
	{
		byte[] data = key.toByteArray();
		int size = data.length;
		out.writeInt(size);
		out.write(data, 0, size);	
	}

	@Override
	public int compareTo(KeyWritable key) 
	{
		return this.key.compareTo( key.getKey() );
	}

}
