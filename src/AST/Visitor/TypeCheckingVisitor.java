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

    private String getParentScopeMethodReturnType(Call call) {
        System.out.println(call.i.toString());
        SymbolTable tmpSymbolTable = st.exitScope();
        return tmpSymbolTable.getMethodTable().get(call.i.toString()).getType();
    }

    private String getIdentifierType(String identifier) {
        try {
            Symbol symbol = st.getVarTable().get(identifier);
            if (symbol != null) {
                return symbol.getType();
            }
            else {
                SymbolTable tmpSymbolTable = st.exitScope();
                symbol = tmpSymbolTable.getVarTable().get(identifier);
                return symbol.getType();
            }
        } catch (NullPointerException nullPointerException) {
            System.out.println("Null pointer exception encountered for: " + identifier);
            return "";
        }
    }

    private boolean validOperatorObjects(Object objectOne, Object objectTwo) {
        if (objectOne instanceof IdentifierExp &&
                (! getIdentifierType(((IdentifierExp) objectOne).s).equals("int"))) {
            return false;
        }
        else if (objectOne instanceof Call && !getParentScopeMethodReturnType((Call) objectOne).equals("int")) {
            return false;
        }
        else if (!(objectOne instanceof IdentifierExp) &&
                !(objectOne instanceof Call) &&
                !(objectOne instanceof IntegerLiteral) &&
                !(objectOne instanceof Plus) &&
                !(objectOne instanceof Minus) &&
                !(objectOne instanceof Times)) {
            return false;
        }
        if (objectTwo instanceof IdentifierExp &&
                (! getIdentifierType(((IdentifierExp) objectTwo).s).equals("int"))) {
            return false;
        }
        else if (objectTwo instanceof Call && !getParentScopeMethodReturnType((Call) objectTwo).equals("int")) {
            return false;
        }
        else if (!(objectTwo instanceof IdentifierExp) &&
                !(objectTwo instanceof Call) &&
                !(objectTwo instanceof IntegerLiteral) &&
                !(objectTwo instanceof Plus) &&
                !(objectTwo instanceof Minus) &&
                !(objectTwo instanceof Times)) {
            return false;
        }
        return true;
    }

    private boolean validControlExpression(Object object) {
        if (object instanceof IdentifierExp &&
                (! getIdentifierType(((IdentifierExp) object).s).equals("boolean"))) {
            return false;
        }
        else if (object instanceof Call && !getParentScopeMethodReturnType((Call) object).equals("boolean")) {
            return false;
        }
        else if ((!(object instanceof IdentifierExp)) &&
                (!(object instanceof Call)) &&
                ((!(object instanceof BooleanType)) &&
                        (!(object instanceof And)) &&
                        (!(object instanceof False)) &&
                        (!(object instanceof True)) &&
                        (!(object instanceof LessThan)) &&
                        (!(object instanceof Not)))) {
            return false;
        }
        return true;
    }

    // Display added for toy example language. Not used in regular MiniJava
    public Object visit(Display n) {
        n.e.accept(this);
        return n;
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
        st = st.enterScope(n.i1.toString());
        n.i1.accept(this);
        n.i2.accept(this);
        n.s.accept(this);
        st = st.exitScope();
        return n;
    }

    // Identifier i;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Object visit(ClassDeclSimple n) {
        // not already declared?
        st = st.enterScope(n.i.toString());
        n.i.accept(this);
        if (n.vl != null) {
            for (int i = 0; i < n.vl.size(); i++) {
                n.vl.get(i).accept(this);
            }
        }
        if (n.ml != null) {
            for (int i = 0; i < n.ml.size(); i++) {
                n.ml.get(i).accept(this);
            }
        }
        st = st.exitScope();
        return n;
    }

    // Identifier i;
    // Identifier j;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Object visit(ClassDeclExtends n) {
        // extending a valid class?
        // not already declared?
        st = st.enterScope(n.i.toString());
        n.i.accept(this);
        n.j.accept(this);
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.get(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.get(i).accept(this);
        }
        st = st.exitScope();
        return n;
    }

    // Type t;
    // Identifier i;
    public Object visit(VarDecl n) {
        // check that it doesn't already exist
        n.t.accept(this);
        n.i.accept(this);
        return n;
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
        st = st.enterScope(n.i.toString());
        n.i.accept(this);
        if (n.fl != null) {
            for (int i = 0; i < n.fl.size(); i++) {
                n.fl.get(i).accept(this);
            }
        }
        if (n.vl != null) {
            for (int i = 0; i < n.vl.size(); i++) {
                n.vl.get(i).accept(this);
            }
        }
        if (n.sl != null) {
            for (int i = 0; i < n.sl.size(); i++) {
                n.sl.get(i).accept(this);
            }
        }
        n.e.accept(this);
        st = st.exitScope();
        return n;
    }

    // Type t;
    // Identifier i;
    public Object visit(Formal n) {
        // valid types?
        n.t.accept(this);
        n.i.accept(this);
        return n;
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
        return n;
    }

    // Exp e;
    // Statement s1,s2;
    public Object visit(If n) {
        // also need to check if a method call returns a boolean or if an identifier is boolean
        if (!validControlExpression(n.e.accept(this))) {
            report_error(n.line_number, "If statement exprects a boolean expression");
        }
        n.s1.accept(this);
        n.s2.accept(this);
        return n;
    }

    // Exp e;
    // Statement s;
    public Object visit(While n) {
        // also check identifier
        if (!validControlExpression(n.e.accept(this))) {
            report_error(n.line_number, "If statement exprects a boolean expression");
        }
        n.s.accept(this);
        return n;
    }

    // Exp e;
    public Object visit(Print n) {
        // should print string
        n.e.accept(this);
        return n;
    }

    // Identifier i;
    // Exp e;
    public Object visit(Assign n) {
        // compare types
        n.i.accept(this);
        n.e.accept(this);
        return n;
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
        return n;
    }

    // Exp e1,e2;
    public Object visit(And n) {
        // should also check if it's an identifier
        // if it is should check type
        Object e1Object = n.e1.accept(this);
        Object e2Object = n.e2.accept(this);
        if (e1Object instanceof IdentifierExp &&
                (! getIdentifierType(((IdentifierExp) e1Object).s).equals("boolean"))) {
            report_error(n.line_number, "Invalid type for first argument in And expression");
        }
        else if (e1Object instanceof Call && !getParentScopeMethodReturnType((Call) e1Object).equals("boolean")) {
            report_error(n.line_number, "Method in and statement doesn't return a boolean");
        }
        else if (!(e2Object instanceof IdentifierExp) &&
                !(e2Object instanceof BooleanType) &&
                !(e2Object instanceof Call) &&
                !(e2Object instanceof LessThan) &&
                !(e2Object instanceof Not) &&
                !(e2Object instanceof And)) {
            report_error(n.line_number, "Invalid type for first argument in And expression");
        }
        if (e2Object instanceof IdentifierExp &&
                (! getIdentifierType(((IdentifierExp) e2Object).s).equals("boolean"))) {
            report_error(n.line_number, "Invalid type for second argument in And expression");
        }
        else if (e2Object instanceof Call && !getParentScopeMethodReturnType((Call) e2Object).equals("boolean")) {
            report_error(n.line_number, "Method in and statement doesn't return a boolean");
        }
        else if (!(e2Object instanceof IdentifierExp) &&
                !(e2Object instanceof BooleanType) &&
                !(e2Object instanceof Call) &&
                !(e2Object instanceof LessThan) &&
                !(e2Object instanceof Not) &&
                !(e2Object instanceof And)) {
            report_error(n.line_number, "Invalid type for second argument in And expression");
        }
        return n;
    }

    // Exp e1,e2;
    public Object visit(LessThan n) {
        // should also check if it's an identifier
        // if it is should check type
        if (!validOperatorObjects(n.e1.accept(this), n.e2.accept(this))) {
            report_error(n.line_number, "Invalid argument in less than expression");
        }
        return n;
    }

    // Exp e1,e2;
    public Object visit(Plus n) {
        // should also check if it's an identifier
        // if it is should check type
        if (!validOperatorObjects(n.e1.accept(this), n.e2.accept(this))) {
            report_error(n.line_number, "Invalid argument in addition expression");
        }
        return n;
    }

    // Exp e1,e2;
    public Object visit(Minus n) {
        // should also check if it's an identifier
        // if it is should check type
        if (!validOperatorObjects(n.e1.accept(this), n.e2.accept(this))) {
            report_error(n.line_number, "Invalid argument in subtraction expression");
        }
        return n;
    }

    // Exp e1,e2;
    public Object visit(Times n) {
        // should also check if it's an identifier
        // if it is should check type
        if (!validOperatorObjects(n.e1.accept(this), n.e2.accept(this))) {
            report_error(n.line_number, "Invalid argument in multiplication expression");
        }
        return n;
    }

    // Exp e1,e2;
    public Object visit(ArrayLookup n) {
        Object e1Object = n.e1.accept(this);
        Object e2Object = n.e2.accept(this);
        if (!(e1Object instanceof IdentifierExp)) {
            report_error(n.line_number, "Invalid identifier for array lookup");
        }
        if (e2Object instanceof IdentifierExp &&
                (! getIdentifierType(((IdentifierExp) e2Object).s).equals("int"))) {
            report_error(n.line_number, "Invalid array index");
        }
        else if (!(e2Object instanceof IdentifierExp) &&
                !(e2Object instanceof IntegerLiteral)) {
            report_error(n.line_number, "Invalid array index");
        }
        return n;
    }

    // Exp e;
    public Object visit(ArrayLength n) {
        if (! (n.e.accept(this) instanceof IdentifierExp)) {
            report_error(n.line_number, "Attempts to get length of invalid identifier");
        }
        return n;
    }

    // Exp e;
    // Identifier i;
    // ExpList el;
    public Object visit(Call n) {
        Object eObject = n.e.accept(this);
        if (!(eObject instanceof IdentifierExp || eObject instanceof NewObject || eObject instanceof This
        || eObject instanceof Call)) {
            report_error(n.line_number, "Attempts to call method with invalid identifier: "
            + eObject.getClass().getName());
        }
        n.i.accept(this);
        if (n.el != null) {
            // maybe check argument length and compare to method decl
            for (int i = 0; i < n.el.size(); i++) {
                // Need to check that these are valid arguments - types
                n.el.get(i).accept(this);
            }
        }
        return n;
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
        if (n.e.accept(this) instanceof IdentifierExp &&
                (! getIdentifierType(((IdentifierExp) n.e.accept(this)).s).equals("int"))) {
            report_error(n.line_number, "Non-integer literal in new array declaration");
        }
        else if (!(n.e.accept(this) instanceof IdentifierExp) &&
                !(n.e.accept(this) instanceof IntegerType)) {
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
        Object eObject = n.e.accept(this);
        if (eObject instanceof IdentifierExp &&
                (! getIdentifierType(((IdentifierExp) eObject).s).equals("boolean"))) {
            report_error(n.line_number, "Operator '!' used on a non-boolean type");
        }
        else if (eObject instanceof Call && !getParentScopeMethodReturnType((Call) eObject).equals("boolean")) {
            report_error(n.line_number, "Method in not statement doesn't return a boolean");
        }
        else if (!(eObject instanceof IdentifierExp) &&
                !(eObject instanceof Call) &&
                !(eObject instanceof BooleanType) &&
                !(eObject instanceof LessThan) &&
                !(eObject instanceof Not) &&
                !(eObject instanceof And)) {
            report_error(n.line_number, "Operator '!' used on a non-boolean type");
        }
        return n;
    }

    // String s;
    public Object visit(Identifier n) {
        return n;
    }
}
