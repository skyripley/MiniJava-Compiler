package AST.syntaxtree.visitor.ops;

import AST.syntaxtree.visitor.ops.visitor.IrVisitor;

public class Return implements Statement
{
	private Expression operand;
	public Return(Expression operand)
	{
		this.operand = operand;
	}
	
	public Expression getSource()
	{
		return operand;
	}
	
	@Override
	public void accept(IrVisitor visitor)
	{
		visitor.visit(this);		
	}
	
	@Override
	public String toString()
	{
		return "return " + operand.toString();
	}
}
