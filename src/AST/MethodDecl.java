package AST;

import AST.Visitor.ObjectVisitor;
import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class MethodDecl extends ASTNode {
  public Type t;
  public Identifier i;
  public FormalList fl;
  public VarDeclList vl;
  public StatementList sl;
  public Exp e;

  public MethodDecl(Type at, Identifier ai, FormalList afl, VarDeclList avl, 
                    StatementList asl, Exp ae, Location pos) {
    super(pos);
    t=at; i=ai; fl=afl; vl=avl; sl=asl; e=ae;
  }

  public MethodDecl(Type at, Identifier ai, VarDeclList avl, StatementList asl, Exp ae, Location pos) {
    super(pos);
    t = at;
    i = ai;
    vl = avl;
    sl = asl;
    e = ae;
  }

  public MethodDecl(Type at, Identifier ai, FormalList afl, StatementList asl, Exp ae, Location pos) {
    super(pos);
    t = at;
    i = ai;
    fl = afl;
    sl = asl;
    e = ae;
  }

  public MethodDecl(Type at, Identifier ai, StatementList asl, Exp ae, Location pos) {
    super(pos);
    t = at;
    i = ai;
    sl = asl;
    e = ae;
  }

  public MethodDecl(Type at, Identifier ai, FormalList afl, Exp ae, Location pos) {
    super(pos);
    t = at;
    i = ai;
    fl = afl;
    e = ae;
  }

  public MethodDecl(Type at, Identifier ai, Exp ae, Location pos) {
    super(pos);
    t = at;
    i = ai;
    e = ae;
  }
 
  public void accept(Visitor v) {
    v.visit(this);
  }

  public Object accept(ObjectVisitor objectVisitor) { return objectVisitor.visit(this); }
}
