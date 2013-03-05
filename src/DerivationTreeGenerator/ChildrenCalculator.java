package DerivationTreeGenerator;

import java.util.List;

import Interfaces.AbstractDatabase;

public interface ChildrenCalculator 
{
	public List<int []> findAllChildren(int[] multiset, String[] membranes, AbstractDatabase db);
}
