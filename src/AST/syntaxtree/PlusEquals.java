package AST.syntaxtree;

import AST.syntaxtree.visitor.TypeVisitor;
import AST.syntaxtree.visitor.Visitor;

public class PlusEquals extends Statement
{
    public Identifier id;
    public Exp e;
    
    public PlusEquals(Identifier id, Exp e) { 
      this.id = id;
      this.e = e;
    }

    public void accept(Visitor v) {
      v.visit(this);
    }

    @Override
    public Type accept(TypeVisitor v)
    {
        return v.visit(this);
    }
}
