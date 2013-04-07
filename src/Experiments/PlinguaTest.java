package Experiments;

import java.io.FileInputStream;

import org.gcn.plinguacore.parser.AbstractParserFactory;
import org.gcn.plinguacore.parser.input.InputParser;
import org.gcn.plinguacore.parser.input.InputParserFactory;
import org.gcn.plinguacore.simulator.ISimulator;
import org.gcn.plinguacore.util.psystem.Psystem;

public class PlinguaTest 
{

	public static void main(String[] args) 
	{
		String fileName="/home/oracle/workspace/psystem/tools/Psystem.pli";
		try
		{
			FileInputStream stream = new FileInputStream(fileName);
			AbstractParserFactory pf = new InputParserFactory();
			InputParser parser = (InputParser) pf.createParser("P-Lingua");
			Psystem ps = parser.parse(stream);
			
			ISimulator sim = ps.createSimulator(false, false);
			sim.runSteps(5);
			
			System.out.println(ps.getRules().toString());
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		

	}

}
