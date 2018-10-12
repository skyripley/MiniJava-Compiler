package AST;

import AST.Visitor.ObjectVisitor;
import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class Block extends Statement {
  public StatementList sl;

  public Block(StatementList asl, Location pos) {
    super(pos);
    sl=asl;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Object accept(ObjectVisitor objectVisitor) { return objectVisitor.visit(this); }
}

