package AST.Visitor;

import AST.*;
import Symtab.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TypeCheckingVisitor implements ObjectVisitor {

    private SymbolTable st = null;
    public int errors = 0;

    public void setSymtab(SymbolTable s)
    {
        st = s;
    }

    public SymbolTable getSymtab()
    {
        return st;
    }

    private void report_error(int line, String msg)
    {
        System.out.println(line+": "+msg);
        ++errors;
    }

    private boolean validMethodCall(Call call, String expectedType) {
        Object object = call.e.accept(this);
        if (object instanceof IdentifierExp) {
            if (!validReferenceType((IdentifierExp) object, call.i.toString())) {
                return false;
            }
            if (!validReturnType(call.i.toString(), expectedType)) {
                return false;
            }
            if (call.el != null) {
                if (!validMethodParameters(call.i.toString(), call.el, getIdentifierType(((IdentifierExp) object).s))) {
                    return false;
                }
            }
        }
        else if (object instanceof This) {
            if (!validReturnType(call.i.toString(), expectedType)) {
                return false;
            }
            if (call.el != null) {
                if (!validMethodParameters(call.i.toString(), call.el, st.getParent().getScopeName())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validReferenceType(IdentifierExp identifierExp, String methodName) {
        String identifier = identifierExp.s;
        String identifierType = null;
        SymbolTable tmpSymbolTable = st;
        Symbol identifierVar = tmpSymbolTable.getVarTable().get(identifier);
        if (identifierVar != null) {
            identifierType = identifierVar.getType();
        }
        else {
            return false;
        }
        while (tmpSymbolTable.getParent() != null) {
            tmpSymbolTable = tmpSymbolTable.getParent();
        }
        if (tmpSymbolTable.getClassTable().get(identifierType) != null) {
            tmpSymbolTable = tmpSymbolTable.enterScope(identifierType);
            if (tmpSymbolTable.getMethodTable().get(methodName) != null) {
                return true;
            }
        }
        return false;
    }

    private boolean validReturnType(String methodName, String expectedType) {
        try {
            return getMethodReturnType(methodName).equals(expectedType);
        } catch (NullPointerException ex) {
            return false;
        }
    }

    private boolean validMethodParameters(String methodName, ExpList expList, String scopeName) {
        boolean result = true;
        ArrayList<Symbol> methodParameters = new ArrayList<>();
        SymbolTable tmpSymbolTable = st;
        while(tmpSymbolTable.getParent() != null) {
            tmpSymbolTable = tmpSymbolTable.getParent();
        }
        tmpSymbolTable = tmpSymbolTable.enterScope(scopeName);
        HashMap<String, Symbol> methodTable = tmpSymbolTable.getMethodTable();
        Symbol methodSymbol = methodTable.get(methodName);
        if (methodSymbol != null) {
            methodParameters = ((MethodSymbol) methodSymbol).getParameters();
        }
        else {
            result = false;
        }
        if (expList != null && expList.size() == methodParameters.size()) {
            int size = expList.size();
            for (int i = 0; i < size; i++) {
                String parameterType = ((VarSymbol) methodParameters.get(i)).getType();
                Exp exp = expList.get(i);
                Object object = exp.accept(this);
                String objectType = getExpressionType(object);
                if (!(objectType != null && objectType.equals(parameterType))) {
                    if (((ClassSymbol) tmpSymbolTable.exitScope().getClassTable().get(objectType)).getType().equals(parameterType)) {
                        continue;
                    }
                    result = false;
                    break;
                }
            }
        }
        else if (expList == null && methodParameters.size() == 0) {
            result = true;
        }
        else {
            result = false;
        }
        return result;
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
            return "";
        }
    }

    private String getExpressionType(Object object) {
        String objectName = object.getClass().getName().replace("AST.", "");
        switch (objectName) {
            case "And": return "boolean";
            case "ArrayLength": return "int";
            case "ArrayLookup": return getExpressionType(((ArrayLookup) object).e1);
            case "BooleanType": return "boolean";
            case "Call": return getMethodReturnType(((Call) object).i.toString());
            case "False": return "boolean";
            case "True": return "boolean";
            case "IdentifierExp": return getIdentifierType(((IdentifierExp) object).s);
            case "IntArrayType": return "int[]";
            case "IntegerLiteral": return "int";
            case "LessThan": return "boolean";
            case "Minus": return "int";
            case "NewArray": return getExpressionType(((NewArray) object).e);
            case "NewObject": return ((NewObject) object).i.toString();
            case "Not": return "boolean";
            case "Plus": return "int";
            case "Times": return "int";
            case "This": String scopeName = st.getParent().getScopeName();
                         if (scopeName.equals("Global")) {
                             scopeName = st.getScopeName();
                         }
                         return scopeName;
        }
        return null;
    }

    private String getTypeString(Type t) {
        String type = t.toString().replace("AST.", "").split("@")[0];
        switch (type) {
            case "BooleanType": return "boolean";
            case "IdentifierType": return ((IdentifierType) t).s;
            case "IntArrayType": return "int[]";
            case "IntegerType": return "int";
        }
        return null;
    }

    private String getMethodReturnType(String methodName) {
        SymbolTable symbolTable = st;
        Symbol methodSymbol = st.getMethodTable().get(methodName);
        if (methodSymbol != null) {
            return methodSymbol.getType();
        }
        // move to global scope to search for method
        while (symbolTable.getParent() != null) {
            symbolTable = symbolTable.getParent();
        }
        HashMap<String, Symbol> classes = symbolTable.getClassTable();
        for (String key : classes.keySet()) {
            SymbolTable tmpSymbolTable = symbolTable.enterScope(key);
            if (tmpSymbolTable.getMethodTable().get(methodName) != null) {
                return tmpSymbolTable.getMethodTable().get(methodName).getType();
            }
        }
        return null;
    }

    private boolean validOperatorObjects(Object objectOne) {
        if (objectOne instanceof IdentifierExp &&
                (!getIdentifierType(((IdentifierExp) objectOne).s).equals("int"))) {
            return false;
        }
        else if (objectOne instanceof Call && !validMethodCall((Call) objectOne, "int")) {
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
        return true;
    }

    private boolean validControlExpression(Object object) {
        if (object instanceof IdentifierExp &&
                (!getIdentifierType(((IdentifierExp) object).s).equals("boolean"))) {
            return false;
        }
        else if (object instanceof Call && !validMethodCall((Call) object, "boolean")) {
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
        return n;
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
        if (!getExpressionType(n.e).equals(getTypeString(n.t))) {
            report_error(n.line_number, "Return type mismatch");
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
        if (!validControlExpression(n.e.accept(this))) {
            report_error(n.line_number, "If statement expects a boolean expression");
        }
        if (n.s1 != null) {
            n.s1.accept(this);
        }
        if (n.s2 != null) {
            n.s2.accept(this);
        }
        return n;
    }

    // Exp e;
    // Statement s;
    public Object visit(While n) {
        if (!validControlExpression(n.e.accept(this))) {
            report_error(n.line_number, "If statement expects a boolean expression");
        }
        n.s.accept(this);
        return n;
    }

    // Exp e;
    public Object visit(Print n) {
        n.e.accept(this);
        return n;
    }

    // Identifier i;
    // Exp e;
    public Object visit(Assign n) {
        String identifierType = getIdentifierType(n.i.toString());
        String expressionType = getExpressionType(n.e);
        if (expressionType == null ||
                !identifierType.replace("[]", "").equals(expressionType.replace("[]", ""))) {
            report_error(n.line_number, "Mismatched assignment. Cannot assign " + expressionType + " to " + identifierType);
        }
        n.i.accept(this);
        n.e.accept(this);
        return n;
    }

    // Identifier i;
    // Exp e1,e2;
    public Object visit(ArrayAssign n) {
        String identifierType = getIdentifierType(n.i.toString());
        String exp1Type = getExpressionType(n.e1);
        String exp2Type = getExpressionType(n.e2);
        if (exp1Type == null || !exp1Type.equals("int")) {
            report_error(n.line_number, "Invalid array index type. Expect int found: " + exp1Type);
        }
        if (exp2Type == null || !exp2Type.replace("[]", "").equals(identifierType.replace("[]", ""))) {
            report_error(n.line_number, "Invalid array item assignment. Expected: "
            + identifierType.replace("[]", ""));
        }
        n.i.accept(this);
        n.e1.accept(this);
        n.e2.accept(this);
        return n;
    }

    // Exp e1,e2;
    public Object visit(And n) {
        Object e1Object = n.e1.accept(this);
        Object e2Object = n.e2.accept(this);
        if (!andExpressionHelper(e1Object)) {
            report_error(n.line_number, "Invalid type for first argument in And expression");
        }
        if (!andExpressionHelper(e2Object)) {
            report_error(n.line_number, "Invalid type for second argument in And expression");
        }
        return n;
    }

    private boolean andExpressionHelper(Object object) {
        if (object instanceof IdentifierExp &&
                (! getIdentifierType(((IdentifierExp) object).s).equals("boolean"))) {
            return false;
        }
        else if (object instanceof Call && !validMethodCall((Call) object, "boolean")) {
            return false;
        }
        else if (!(object instanceof IdentifierExp) &&
                !(object instanceof BooleanType) &&
                !(object instanceof Call) &&
                !(object instanceof LessThan) &&
                !(object instanceof Not) &&
                !(object instanceof And)) {
            return false;
        }
        return true;
    }

    // Exp e1,e2;
    public Object visit(LessThan n) {
        // should also check if it's an identifier
        // if it is should check type
        if (!validOperatorObjects(n.e1.accept(this))) {
            report_error(n.line_number, "Argument one in less than expression invalid");
        }
        if (!validOperatorObjects(n.e2.accept(this))) {
            report_error(n.line_number, "Argument two in less than expression invalid");
        }
        return n;
    }

    // Exp e1,e2;
    public Object visit(Plus n) {
        // should also check if it's an identifier
        // if it is should check type
        if (!validOperatorObjects(n.e1.accept(this))) {
            report_error(n.line_number, "Argument one in addition expression invalid");
        }
        if (!validOperatorObjects(n.e2.accept(this))) {
            report_error(n.line_number, "Argument two in addition expression invalid");
        }
        return n;
    }

    // Exp e1,e2;
    public Object visit(Minus n) {
        // should also check if it's an identifier
        // if it is should check type
        if (!validOperatorObjects(n.e1.accept(this))) {
            report_error(n.line_number, "Argument one in subtraction expression invalid");
        }
        if (!validOperatorObjects(n.e2.accept(this))) {
            report_error(n.line_number, "Argument two in subtraction expression invalid");
        }
        return n;
    }

    // Exp e1,e2;
    public Object visit(Times n) {
        // should also check if it's an identifier
        // if it is should check type
        if (!validOperatorObjects(n.e1.accept(this))) {
            report_error(n.line_number, "Argument one in multiplication expression invalid");
        }
        if (!validOperatorObjects(n.e2.accept(this))) {
            report_error(n.line_number, "Argument two in multiplication expression invalid");
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
        String refType = "";
        if (eObject instanceof IdentifierExp) {
            refType = getIdentifierType(((IdentifierExp) eObject).s);
        }
        else if (eObject instanceof NewObject) {
            refType = ((NewObject) eObject).i.toString();
        }
        else if (eObject instanceof This) {
            refType = st.getParent().getScopeName();
        }
        else if (eObject instanceof Call) {
            refType = getMethodReturnType(((Call) eObject).i.toString());
        }
        if (!(validMethodParameters(n.i.toString(), n.el, refType))) {
            report_error(n.line_number, "Invalid arguments passed to method " + n.i.toString());
        }
        SymbolTable tmpSymbolTable = st;
        while (tmpSymbolTable.getParent() != null) {
            tmpSymbolTable = tmpSymbolTable.getParent();
        }
        tmpSymbolTable = tmpSymbolTable.enterScope(refType);
        if (tmpSymbolTable.getMethodTable().get(n.i.toString()) == null) {
            report_error(n.line_number, "Method " + n.i.toString() + " is undefined");
        }
        n.i.accept(this);
        if (n.el != null) {
            for (int i = 0; i < n.el.size(); i++) {
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
        if (st.getVarTable().get(n.s) == null && st.getParent().getVarTable().get(n.s) == null) {
            report_error(n.line_number, "Var " + n.s + " hasn't been declared");
        }
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
        if (st.lookupSymbol(n.i.toString()) == null) {
            report_error(n.line_number, n.i.toString() + " doesn't exist - can't be instantiated");
        }
        return n;
    }

    // Exp e;
    public Object visit(Not n) {
        if (!validControlExpression(n.e.accept(this))) {
            report_error(n.line_number, "Operator '!' used on a non-boolean type");
        }
        return n;
    }

    // String s;
    public Object visit(Identifier n) {
        return n;
    }
}
