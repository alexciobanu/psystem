package Interfaces;

import java.util.ArrayList;
import java.util.List;

import oracle.kv.Value;

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
	
	public void StoreNode(String NodeId, int level, String membrane, NodeData theNode);
	
	void StoreAppliedRules(String NodeId, Object data);
	
	void StoreChildren(String NodeId,  ArrayList<String> data);
	
    Object RetriveAppliedRules(String NodeId);
	
	Object RetrieveChildren(String NodeId);
	
	public Value createNodeValue(NodeData theNode);
	
	NodeData RetrieveNode(String NodeId, int level, String membrane);
	
	List<String> retriveLevelIDs(int level, String membrane);
	
	void deletePsystem();

	void deleteLevel(int level);
	
	void printLevelSize(int level);
	
	void deleteAllTemp();
	
	void printAllElements();
	
	public List<String> RetriveMembraneSolutionConstants(String membraneID);
	
	public float[][] RetriveMembraneSolutionMatrix(String membraneID);
	
	public boolean CheckForSolutionMatrix(String membraneID);
	
	public void StoreMembraneSolutionMatrix(String membraneID, float[][] solutionsMatrix, List<String> objects);
	
	public void storeCDRCPair(int producerRule, String membranes, int consumerRule, String membranes2);
	
	int retrieveAllCDRCPair(boolean suppressOutput);
	
	public boolean checkAndRemoveCDRCPair(int producerRule, int producerMembrane, int consumerRule, int consumerMembrane);



}
