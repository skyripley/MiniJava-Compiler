package AST.Visitor;

import AST.*;
import Symtab.*;

public class TypeCheckingVisitor implements ObjectVisitor {

    SymbolTable st = null;
    public int errors = 0;
    public int warnings = 0;

    public void setSymtab(SymbolTable s)
    {
        st = s;
    }

    public SymbolTable getSymtab()
    {
        return st;
    }

    public void report_error(int line, String msg)
    {
        System.out.println(line+": "+msg);
        ++errors;
    }

    public void report_warning(int line, String msg)
    {
        System.out.println(line+": "+msg);
        ++warnings;
    }

    private boolean isAssignable(Type type, Type typeOne) {
        return type.getClass().equals(typeOne.getClass());
    }

    private boolean sameType(Type type, Type typeOne) {
        return type.getClass().equals(typeOne.getClass());
    }

    // Maybe add method to get identifier type

    // Display added for toy example language. Not used in regular MiniJava
    public Object visit(Display n) {
        n.e.accept(this);
        return null;
    }

    // MainClass m;
    // ClassDeclList cl;
    public Object visit(Program n) {
        n.m.accept(this);
        for (int i = 0; i < n.cl.size(); i++) {
            n.cl.get(i).accept(this);
        }
        return "This is a program";
    }

    // Identifier i1,i2;
    // Statement s;
    public Object visit(MainClass n) {
        // not already declared?
        n.i1.accept(this);
        n.i2.accept(this);
        n.s.accept(this);
        return null;
    }

    // Identifier i;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Object visit(ClassDeclSimple n) {
        // not already declared?
        n.i.accept(this);
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.get(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.get(i).accept(this);
        }
        return null;
    }

    // Identifier i;
    // Identifier j;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Object visit(ClassDeclExtends n) {
        // extending a valid class?
        // not already declared?
        n.i.accept(this);
        n.j.accept(this);
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.get(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.get(i).accept(this);
        }
        return null;
    }

    // Type t;
    // Identifier i;
    public Object visit(VarDecl n) {
        // check that it doesn't already exist
        n.t.accept(this);
        n.i.accept(this);
        return null;
    }

    // Type t;
    // Identifier i;
    // FormalList fl;
    // VarDeclList vl;
    // StatementList sl;
    // Exp e;
    public Object visit(MethodDecl n) {
        n.t.accept(this);
        // method shouldn't already exist in scope unless overridden
        // if overriden types should match
        n.i.accept(this);
        for (int i = 0; i < n.fl.size(); i++) {
            n.fl.get(i).accept(this);
        }
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.get(i).accept(this);
        }
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.get(i).accept(this);
        }
        n.e.accept(this);
        return null;
    }

    // Type t;
    // Identifier i;
    public Object visit(Formal n) {
        // valid types?
        n.t.accept(this);
        n.i.accept(this);
        return null;
    }

    public Object visit(IntArrayType n) {
        return n;
    }

    public Object visit(BooleanType n) {
        return n;
    }

    public Object visit(IntegerType n) {
        return n;
    }

    // String s;
    public Object visit(IdentifierType n) {
        return n;
    }

    // StatementList sl;
    public Object visit(Block n) {
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.get(i).accept(this);
        }
        return null;
    }

    // Exp e;
    // Statement s1,s2;
    public Object visit(If n) {
        // also check identifier
        if (! (n.e.accept(this) instanceof BooleanType)) {
            report_error(n.line_number, "If statement expects boolean");
        }
        n.s1.accept(this);
        n.s2.accept(this);
        return null;
    }

    // Exp e;
    // Statement s;
    public Object visit(While n) {
        // also check identifier
        if (! (n.e.accept(this) instanceof BooleanType)) {
            report_error(n.line_number, "While statement expects boolean");
        }
        n.s.accept(this);
        return null;
    }

    // Exp e;
    public Object visit(Print n) {
        // should print string
        n.e.accept(this);
        return null;
    }

    // Identifier i;
    // Exp e;
    public Object visit(Assign n) {
        // compare types
        n.i.accept(this);
        n.e.accept(this);
        return null;
    }

    // Identifier i;
    // Exp e1,e2;
    public Object visit(ArrayAssign n) {
        // should check for array out of bounds
        // should check that valid types are being assigned
        // should check that the array has been declared
        n.i.accept(this);
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Exp e1,e2;
    public Object visit(And n) {
        // should also check if it's an identifier
        // if it is should check type
        if (! (n.e1.accept(this) instanceof BooleanType)) {
            report_error(n.line_number, "Invalid type for multiplication");
        }
        if (! (n.e2.accept(this) instanceof BooleanType)) {
            report_error(n.line_number, "Invalid type for multiplication");
        }
        return null;
    }

    // Exp e1,e2;
    public Object visit(LessThan n) {
        // should also check if it's an identifier
        // if it is should check type
        if (! (n.e1.accept(this) instanceof IntegerLiteral)) {
            report_error(n.line_number, "Invalid type for multiplication");
        }
        if (! (n.e2.accept(this) instanceof IntegerLiteral)) {
            report_error(n.line_number, "Invalid type for multiplication");
        }
        return null;
    }

    // Exp e1,e2;
    public Object visit(Plus n) {
        // should also check if it's an identifier
        // if it is should check type
        if (! (n.e1.accept(this) instanceof IntegerLiteral)) {
            report_error(n.line_number, "Invalid type for multiplication");
        }
        if (! (n.e2.accept(this) instanceof IntegerLiteral)) {
            report_error(n.line_number, "Invalid type for multiplication");
        }
        return null;
    }

    // Exp e1,e2;
    public Object visit(Minus n) {
        // should also check if it's an identifier
        // if it is should check type
        if (! (n.e1.accept(this) instanceof IntegerLiteral)) {
            report_error(n.line_number, "Invalid type for multiplication");
        }
        if (! (n.e2.accept(this) instanceof IntegerLiteral)) {
            report_error(n.line_number, "Invalid type for multiplication");
        }
        return null;
    }

    // Exp e1,e2;
    public Object visit(Times n) {
        // should also check if it's an identifier
        // if it is should check type
        if (! (n.e1.accept(this) instanceof IntegerLiteral)) {
            report_error(n.line_number, "Invalid type for multiplication");
        }
        if (! (n.e2.accept(this) instanceof IntegerLiteral)) {
            report_error(n.line_number, "Invalid type for multiplication");
        }
        return null;
    }

    // Exp e1,e2;
    public Object visit(ArrayLookup n) {
        if (! (n.e1.accept(this) instanceof IdentifierExp)) {
            report_error(n.line_number, "Invalid identifier for array lookup");
        }
        if (! (n.e2.accept(this) instanceof IntegerLiteral)) {
            report_error(n.line_number, "Invalid array index");
        }
        return null;
    }

    // Exp e;
    public Object visit(ArrayLength n) {
        if (! (n.e.accept(this) instanceof IdentifierExp)) {
            report_error(n.line_number, "Attempts to get length of invalid identifier");
        }
        return null;
    }

    // Exp e;
    // Identifier i;
    // ExpList el;
    public Object visit(Call n) {
        if (! (n.e.accept(this) instanceof IdentifierExp)) {
            report_error(n.line_number, "Attempts to call method with invalid identifier");
        }
        n.i.accept(this);
        if (n.el != null) {
            // maybe check argument length and compare to method decl
            for (int i = 0; i < n.el.size(); i++) {
                // Need to check that these are valid arguments - types
                n.el.get(i).accept(this);
            }
        }
        return null;
    }

    // int i;
    public Object visit(IntegerLiteral n) {
        return n;
    }

    public Object visit(True n) {
        return n;
    }

    public Object visit(False n) {
        return n;
    }

    // String s;
    public Object visit(IdentifierExp n) {
        return n;
    }

    public Object visit(This n) {
        return n;
    }

    // Exp e;
    public Object visit(NewArray n) {
        if (! (n.e.accept(this) instanceof IntegerType)) {
            report_error(n.line_number, "Non-integer literal in new array declaration");
        }
        return n;
    }

    // Identifier i;
    public Object visit(NewObject n) {
        return n;
    }

    // Exp e;
    public Object visit(Not n) {
        if (! (n.e.accept(this) instanceof BooleanType)) {
            report_error(n.line_number, "Operator '!' used on a non-boolean type");
        }
        return null;
    }

    // String s;
    public Object visit(Identifier n) {
        return n;
    }
}
