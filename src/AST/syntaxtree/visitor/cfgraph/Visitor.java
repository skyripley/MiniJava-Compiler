package AST.syntaxtree.visitor.cfgraph;

public interface Visitor
{
	void visit(BranchCodePoint codePoint);
	void visit(LinearCodePoint codePoint);
}
