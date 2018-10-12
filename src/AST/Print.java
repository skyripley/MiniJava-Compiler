package AST;

import AST.Visitor.ObjectVisitor;
import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class Print extends Statement {
  public Exp e;

  public Print(Exp ae, Location pos) {
    super(pos);
    e=ae; 
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Object accept(ObjectVisitor objectVisitor) { return objectVisitor.visit(this); }
}
