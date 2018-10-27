package AST.syntaxtree.visitor.ops;

import AST.syntaxtree.visitor.ops.visitor.IrVisitor;

public class IntegerLiteral implements Expression
{
	int value;

	public IntegerLiteral(int value) { this.value = value; }	
	public int getValue() { return value; }
	
	public void accept(IrVisitor visitor)
	{
		visitor.visit(this);
	}
	
	@Override
	public String toString()
	{
		return Integer.toString(value);
	}
}
