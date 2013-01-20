package InputFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

public class NoSQLSplit extends InputSplit implements Writable
{
	public String membrane;
	public String uuid;
	
	public NoSQLSplit()
	{
		this.membrane="";
		this.uuid="";
	}
	
	public NoSQLSplit(String membrane, String uuid)
	{
		this.membrane=membrane;
		this.uuid=uuid;
	}
	
	@Override
	public long getLength() throws IOException, InterruptedException 
	{
		return 1;
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
		String  rawString = in.readUTF();
		String[] data = rawString.split(":");
		membrane = data[0];	
		uuid = data[1];
	}

	@Override
	public void write(DataOutput out) throws IOException 
	{
	    out.writeUTF(membrane+":"+uuid);
	}
}
