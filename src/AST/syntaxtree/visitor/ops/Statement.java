package AST.syntaxtree.visitor.ops;

import AST.syntaxtree.visitor.ops.visitor.IrVisitor;

public interface Statement
{
	public void accept(IrVisitor visitor);
}
