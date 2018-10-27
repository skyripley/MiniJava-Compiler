package AST.syntaxtree.visitor.ops;

import AST.syntaxtree.visitor.ops.visitor.IrVisitor;

public class BinOp implements Expression 
{
	public enum Op
	{
		ADD,
		SUBTRACT,
		MULT
	}
	private Op op;	
	private Expression src1;
	private Expression src2;
	
	public BinOp(Op op, Expression src1, Expression src2)
	{
		this.op = op;		
		this.src1 = src1;
		this.src2 = src2;
	}
	
	public Op getOp()
	{
		return op;
	}
	
	public Expression getSrc1()
	{
		return src1;
	}
	
	public Expression getSrc2()
	{
		return src2;
	}
	
	public void accept(IrVisitor visitor)
	{
		visitor.visit(this);
	}
	
	@Override
	public String toString()
	{
		String result = src1.toString();
		switch(op)
		{
		case ADD:
			result += " + ";
			break;
		case SUBTRACT:
			result += " - ";
			break;
		case MULT:
			result += " * ";
			break;
		}
		
		return result + src2.toString();
	}
}
