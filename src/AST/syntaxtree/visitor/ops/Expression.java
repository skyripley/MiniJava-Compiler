package AST.syntaxtree.visitor.ops;

import AST.syntaxtree.visitor.ops.visitor.IrVisitor;

public interface Expression
{	
	void accept(IrVisitor visitor);	
}
