package AST.syntaxtree.visitor.regalloc;

import AST.syntaxtree.visitor.cfgraph.BottomUpVisitor;
import AST.syntaxtree.visitor.cfgraph.BranchCodePoint;
import AST.syntaxtree.visitor.cfgraph.CodePoint;
import AST.syntaxtree.visitor.cfgraph.LinearCodePoint;
import AST.syntaxtree.visitor.ops.Statement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LivenessVisitor extends BottomUpVisitor
{	
	private Map<CodePoint, Set<String>> livenessMap = new HashMap<CodePoint, Set<String>>();
	private Set<String> previousLivenessFlags = new HashSet<String>(); 
	@Override
	public void clear()
	{
		super.clear();
		livenessMap.clear();		
	}

	private StatementVisitor statementVisitor = new StatementVisitor();
	private void setLivenessFlag(Statement statement, CodePoint codePoint)
	{
		statementVisitor.clear();
		statement.accept(statementVisitor);

		Set<String> liveSet = livenessMap.get(codePoint);
		if (liveSet == null)
		{
			liveSet = new HashSet<String>();
			livenessMap.put(codePoint, liveSet);
		}
		if(previousLivenessFlags != null)
		{
			liveSet.addAll(previousLivenessFlags);
			previousLivenessFlags = null;
		}		

		for (String liveId : statementVisitor.getLiveSet())
			liveSet.add(liveId);
		for (String deadId : statementVisitor.getDeadSet())
			liveSet.remove(deadId);
	}

	public Map<CodePoint, Set<String>> getLivenessMap()
	{
		return livenessMap;
	}

	@Override
	protected void beforeParent(CodePoint codePoint)
	{		
		previousLivenessFlags = livenessMap.get(codePoint);
	}

	@Override
	public void visit(LinearCodePoint codePoint)
	{
		setLivenessFlag(codePoint.getStatement(), codePoint);
		super.visit(codePoint);
	}

	@Override
	public void visit(BranchCodePoint codePoint)
	{
		setLivenessFlag(codePoint.getCondition(), codePoint);
		super.visit(codePoint);
	}
}
