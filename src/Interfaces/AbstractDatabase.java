package Interfaces;

import java.util.List;

public interface AbstractDatabase 
{

	void initDatabase(String argv[]);
	
	void storeMembraneElement(String membrane, String element, Object data);
	
	Object retriveMembraneElement(String membrane, String element);
	
	public  MultiMembraneMultiset retriveTempElement(String membrane,String ruleID,boolean using);
	
	public void storeTempElement(String membrane , String ruleID, String membraneApply, boolean using, Object data);
	
	void storeAlphbaet(String[] data);
	
	String[] retriveAlphbaet();
	
	void storeMembraneList(String[] data);
	
	String[] retriveMembraneList();
	
	void StoreNode(String NodeId, int level, String membrane, NodeData theNode);
	
	NodeData RetrieveNode(String NodeId, int level, String membrane);
	
	List<String> retriveLevelIDs(int level, String membrane);
	
	void deletePsystem();

	void deleteLevel(int level);
	
	void printLevelSize(int level);
	
	void deleteAllTemp();
	
	void printAllElements();
	
}
