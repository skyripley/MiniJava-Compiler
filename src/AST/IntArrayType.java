package AST;

import AST.Visitor.ObjectVisitor;
import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class IntArrayType extends Type {
  public IntArrayType(Location pos) {
    super(pos);
  }
  public void accept(Visitor v) {
    v.visit(this);
  }
  public Object accept(ObjectVisitor objectVisitor) { return objectVisitor.visit(this); }
}
