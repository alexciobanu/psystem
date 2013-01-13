package AdminTools;

import java.io.IOException;

public class MainTools 
{	
	/*public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		PsystemInterface ps = new PsystemInterface();
		//ps.printAllKeys();
		ps.printLevelSize("level4");
		//viewLevel("level1");
	}*/
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		if (args.length<1)
		{
			System.out.println("Usage:");
			System.out.println("\t load <PLI File> : Load the PLI file into NOSQL DB");
			System.out.println("\t initCDRC : Calculates all CDRC cmibinations and load int DB");
			System.out.println("\t delete : Deletes the Psystem from the database DB");
			System.out.println("\t deleteLevel <level>: Deletes a level of the derivation tree from DB");
			System.out.println("\t show : Displays the Psystem in the database");
			System.out.println("\t showLevel <level>: displays a level of the derivation tree from DB");
			System.out.println("\t showLevelSize <level>: displays the size of a level in the derivation tree");
			System.out.println("\t showKeys : displays all of the keys in the DB");
		}
		else
		{
			if ("load".equalsIgnoreCase(args[0]))
			{
				if (args[1]!=null)
				{
					System.out.println("loading psystem");
					PsystemInterface ps = new PsystemInterface();
					ps.grabPsystem(args[1]);
					System.out.println("Done loading psystem");
				}
				else
				{
					System.out.println("please specify the PLI file to import");
				}
			}	
			else if ("initCDRC".equalsIgnoreCase(args[0]))
			{
				System.out.println("initializing the CDRC pairs");
				initCDRC();
				System.out.println("Done initializing the CDRC pairs");
			}
			else if ("delete".equalsIgnoreCase(args[0]))
			{
				System.out.println("deleting the P-system from the database");
				deletePsystem();
				System.out.println("Done deleting the P-system from the database");
			}
			else if ("deleteLevel".equalsIgnoreCase(args[0]))
			{
				if (args[1]!=null)
				{
					System.out.println("deleting the derivation tree level from the database");
					deleteTreeLevel(args[1]);
					System.out.println("Done deleting the derivation tree level from the database");
				}
				else
				{
					System.out.println("please specify the level to delete");
				}
			}
			else if ("show".equalsIgnoreCase(args[0]))
			{
				System.out.println("Displaying the P system to screen");
				viewPsystem();
				System.out.println("Done Displaying the P system to screen");
			}
			else if ("showKeys".equalsIgnoreCase(args[0]))
			{
				System.out.println("Displaying the database keys");
				PsystemInterface ps = new PsystemInterface();
				ps.printAllKeys();
				System.out.println("Done Displaying the database keys");
			}
			else if ("showLevel".equalsIgnoreCase(args[0]))
			{
				if (args[1]!=null)
				{
					System.out.println("showing the derivation tree level from the database");
					deleteTreeLevel(args[1]);
					System.out.println("Done showing the derivation tree level from the database");
				}
				else
				{
					System.out.println("please specify the level to display");
				}
			}
			else if ("showLevelSize".equalsIgnoreCase(args[0]))
			{
				if (args[1]!=null)
				{
					System.out.println("showing the derivation tree level size from the database");
					PsystemInterface ps = new PsystemInterface();
			        ps.printLevelSize(args[0]);
					System.out.println("Done showing the derivation tree level size from the database");
				}
				else
				{
					System.out.println("please specify the level to display");
				}
			}
			else
			{
				System.out.println("Option not recoginzed please run command without options to see help menu");
			}
		}
	}
	
	public static void initCDRC()
	{
		FindCdrcPairs cdrc = new FindCdrcPairs();
		cdrc.getContextDependentRules();
	}
	
	public static void deleteTreeLevel(String level) throws IOException, ClassNotFoundException
	{
		PsystemInterface ps = new PsystemInterface();
		ps.deleteLevel(level);
	}
	
	public static void viewLevel(String level) throws IOException, ClassNotFoundException
	{
		PsystemInterface ps = new PsystemInterface();
		ps.printMultiset(level);
        ps.printLevelSize(level);
	}
	
	public static void viewPsystem() throws IOException, ClassNotFoundException
	{
		PsystemInterface ps = new PsystemInterface();
	    ps.printAlphabet();
	    ps.printMembranes();
		ps.printAllRules();
        ps.printMultiset("level1");
	}
	
	public static void deletePsystem() throws IOException, ClassNotFoundException
	{
		PsystemInterface ps = new PsystemInterface();
		ps.deletePsystem();
	}	
}
