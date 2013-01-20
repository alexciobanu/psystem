package AdminTools;

import java.io.IOException;

public class MainTools 
{	
	static PsystemInterface ps;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		if (args.length<3)
		{
			System.out.println("Usage:");
			System.out.println("<store> <host> load <PLI File> : Load the PLI file into NOSQL DB");
			System.out.println("<store> <host> initCDRC : Calculates all CDRC cmibinations and load int DB");
			System.out.println("<store> <host> delete : Deletes the Psystem from the database DB");
			System.out.println("<store> <host> deleteLevel <level>: Deletes a level of the derivation tree");
			System.out.println("<store> <host> show : Displays the Psystem in the database");
			System.out.println("<store> <host> showLevel <level>: displays a level of the derivation tree");
			System.out.println("<store> <host> showLevelSize <level>: displays the level size in derivation tree");
			System.out.println("<store> <host> showKeys : displays all of the keys in the DB");
		}
		else
		{
			ps = new PsystemInterface(args[0],args[1]);
			if ("load".equalsIgnoreCase(args[2]))
			{
				if (args[3]!=null)
				{
					System.out.println("loading psystem");
					
					ps.grabPsystem(args[3]);
					System.out.println("Done loading psystem");
				}
				else
				{
					System.out.println("please specify the PLI file to import");
				}
			}	
			else if ("initCDRC".equalsIgnoreCase(args[2]))
			{
				System.out.println("initializing the CDRC pairs");
				initCDRC(args[0],args[1]);
				System.out.println("Done initializing the CDRC pairs");
			}
			else if ("delete".equalsIgnoreCase(args[2]))
			{
				System.out.println("deleting the P-system from the database");
				deletePsystem();
				System.out.println("Done deleting the P-system from the database");
			}
			else if ("deleteLevel".equalsIgnoreCase(args[2]))
			{
				if (args[3]!=null)
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
			else if ("show".equalsIgnoreCase(args[2]))
			{
				System.out.println("Displaying the P system to screen");
				viewPsystem();
				System.out.println("Done Displaying the P system to screen");
			}
			else if ("showKeys".equalsIgnoreCase(args[2]))
			{
				System.out.println("Displaying the database keys");
				ps.printAllKeys();
				System.out.println("Done Displaying the database keys");
			}
			else if ("showLevel".equalsIgnoreCase(args[2]))
			{
				if (args[3]!=null)
				{
					System.out.println("showing the derivation tree level from the database");
					ps.printMultiset(args[3]);
					System.out.println("Done showing the derivation tree level from the database");
				}
				else
				{
					System.out.println("please specify the level to display");
				}
			}
			else if ("showLevelSize".equalsIgnoreCase(args[2]))
			{
				if (args[3]!=null)
				{
					System.out.println("showing the derivation tree level size from the database");
			        ps.printLevelSize(args[3]);
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
	
	public static void initCDRC(String storeName, String hosts)
	{
		FindCdrcPairs cdrc = new FindCdrcPairs(storeName, hosts);
		cdrc.getContextDependentRules();
	}
	
	public static void deleteTreeLevel(String level) throws IOException, ClassNotFoundException
	{
		ps.deleteLevel(level);
	}
	
	public static void viewLevel(String level) throws IOException, ClassNotFoundException
	{
		ps.printMultiset(level);
        ps.printLevelSize(level);
	}
	
	public static void viewPsystem() throws IOException, ClassNotFoundException
	{
	    ps.printAlphabet();
	    ps.printMembranes();
		ps.printAllRules();
        ps.printMultiset("level1");
	}
	
	public static void deletePsystem() throws IOException, ClassNotFoundException
	{
		ps.deletePsystem();
	}	
}
