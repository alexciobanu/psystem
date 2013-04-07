package AdminTools;

import java.io.IOException;

import Interfaces.AbstractDatabase;
import Interfaces.OracleNoSQLDatabase;

public class MainTools 
{	
	static AbstractDatabase db;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		if (args.length<3)
		{
			System.out.println("Usage:");
			System.out.println("<store> <host> load <PLI File> : Load the PLI file into NOSQL DB");
			System.out.println("<store> <host> initEqSolver : generates Solution Matrices for solver");
			System.out.println("<store> <host> initCDRC : Calculates all CDRC cmibinations and load int DB");
			System.out.println("<store> <host> delete : Deletes the Psystem from the database DB");
			System.out.println("<store> <host> deleteLevel <level>: Deletes a level of the derivation tree");
			System.out.println("<store> <host> show : Displays the Psystem in the database");
			System.out.println("<store> <host> showLevel <level>: displays a level of the derivation tree");
			System.out.println("<store> <host> showLevelSize <level>: displays the level size in derivation tree");
			System.out.println("<store> <host> showKeys : displays all of the keys in the DB");
			System.out.println("<store> <host> showCDRC : displays all of the CDRC pairs in the DB");
			System.out.println("<store> <host> showCDRCSize : displays the number of the CDRC pairs in the DB");
		}
		else
		{
			db = new OracleNoSQLDatabase(args[0],args[1]);
			if ("load".equalsIgnoreCase(args[2]))
			{
				if (args.length==4)
				{
					System.out.println("loading psystem");
					PsystemTools.grabPsystem(args[3],db);
					System.out.println("Done loading psystem");
				}
				else
				{
					System.out.println("please specify the PLI file to import");
				}
			}	
			else if ("initEqSolver".equalsIgnoreCase(args[2]))
			{
				System.out.println("initializing equation solver");
				EquationSolverLoader loader = new EquationSolverLoader();
				loader.findSolutionMatrix(db);
				System.out.println("Done initializing equation solver");
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
				if (args.length==4)
				{
					System.out.println("deleting the derivation tree level from the database");
					db.deleteLevel( Integer.parseInt(args[3]));
					System.out.println("Done deleting the derivation tree level from the database");
				}
				else
				{
					System.out.println("please specify the level to delete");
				}
			}
			else if ("show".equalsIgnoreCase(args[2]))
			{
				viewPsystem();
			}
			else if ("showKeys".equalsIgnoreCase(args[2]))
			{
				db.printAllElements();
			}
			else if ("showLevel".equalsIgnoreCase(args[2]))
			{
				if (args.length==4)
				{
					PsystemTools.printLevel( Integer.parseInt( args[3] ),db);
				}
				else
				{
					System.out.println("please specify the level to display");
				}
			}
			else if ("showLevelSize".equalsIgnoreCase(args[2]))
			{
				if (args.length==4)
				{
					System.out.println("showing the derivation tree level size from the database");
					db.printLevelSize( Integer.parseInt( args[3] ) );
				}
				else
				{
					System.out.println("please specify the level to display");
				}
			}
			else if ("showCDRC".equalsIgnoreCase(args[2]))
			{
				//TODO CRDC 
		        //ps.printCDRCpairs();
			}
			
			else if ("showCDRCSize".equalsIgnoreCase(args[2]))
			{
				//TODO CDRC
				//ps.printCDRCpairsSize();
			}		
			else
			{
				System.out.println("Option not recoginzed please run command without options to see help menu");
			}
		}
	}
	
	public static void initCDRC(String storeName, String hosts)
	{
		//TODO CDRC
		//FindCdrcPairs cdrc = new FindCdrcPairs(storeName, hosts);
		//cdrc.getContextDependentRules();
	}
	
	public static void viewLevel(String level) throws IOException, ClassNotFoundException
	{
		PsystemTools.printLevel(Integer.parseInt(level),db);
		db.printLevelSize(Integer.parseInt(level));
	}
	
	public static void viewPsystem() throws IOException, ClassNotFoundException
	{
		PsystemTools.printPsystem(db);
	}
	
	public static void deletePsystem() throws IOException, ClassNotFoundException
	{
		db.deletePsystem();
	}	
}
