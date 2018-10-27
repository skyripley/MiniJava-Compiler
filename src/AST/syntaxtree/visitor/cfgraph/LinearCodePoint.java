package AST.syntaxtree.visitor.cfgraph;

import AST.syntaxtree.visitor.cfgraph.CodePoint;
import AST.syntaxtree.visitor.ops.Statement;
import AST.syntaxtree.visitor.regalloc.LivenessVisitor;

public class LinearCodePoint extends CodePoint
{
	private Statement statement;
	private CodePoint successor;
	
	public LinearCodePoint(Statement statement)
	{
		this.statement = statement;
	}

	public CodePoint getSuccessor()
	{
		return successor;
	}

	public void setSuccessor(CodePoint successor)
	{
		this.successor = successor;
	}
	
	public Statement getStatement()
	{
		return statement;
	}
	
	@Override
	public void accept(Visitor v)
	{
		v.visit(this);
	}
	
	@Override
	public String toString()
	{
		return statement.toString();
	}
}
