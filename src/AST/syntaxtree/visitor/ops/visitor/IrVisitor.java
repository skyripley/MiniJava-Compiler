package AST.syntaxtree.visitor.ops.visitor;

import AST.syntaxtree.visitor.ops.ArrayAccess;
import AST.syntaxtree.visitor.ops.ArrayAllocation;
import AST.syntaxtree.visitor.ops.ArrayLength;
import AST.syntaxtree.visitor.ops.Assignment;
import AST.syntaxtree.visitor.ops.BinOp;
import AST.syntaxtree.visitor.ops.Call;
import AST.syntaxtree.visitor.ops.ConditionalJump;
import AST.syntaxtree.visitor.ops.FunctionDeclaration;
import AST.syntaxtree.visitor.ops.Identifier;
import AST.syntaxtree.visitor.ops.IntegerLiteral;
import AST.syntaxtree.visitor.ops.Jump;
import AST.syntaxtree.visitor.ops.Label;
import AST.syntaxtree.visitor.ops.RecordAccess;
import AST.syntaxtree.visitor.ops.RecordAllocation;
import AST.syntaxtree.visitor.ops.RecordDeclaration;
import AST.syntaxtree.visitor.ops.RelationalOp;
import AST.syntaxtree.visitor.ops.Return;
import AST.syntaxtree.visitor.ops.SysCall;

public interface IrVisitor
{
	void visit(ArrayAccess a);	
	void visit(ArrayAllocation n);
	void visit(ArrayLength a);
	void visit(Assignment assignment);
	void visit(BinOp b);
	void visit(Call c);
	void visit(ConditionalJump j);
	void visit(FunctionDeclaration f);	
	void visit(Identifier i);	
	void visit(IntegerLiteral l);
	void visit(Jump j);
	void visit(Label l);
	void visit(RecordAccess r);	
	void visit(RecordAllocation a);
	void visit(RecordDeclaration r);
	void visit(RelationalOp r);
	void visit(Return r);	
	void visit(SysCall s);
}
