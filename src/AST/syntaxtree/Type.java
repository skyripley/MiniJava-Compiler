package AST.syntaxtree;
import AST.syntaxtree.visitor.TypeVisitor;
import AST.syntaxtree.visitor.Visitor;

public abstract class Type {
  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
}
