package AST.Visitor;

import java.util.HashMap;
import java.util.Iterator;

import AST.*;
import Symtab.*;


public class SymTableVisitor implements Visitor {

	SymbolTable st = new SymbolTable();
	
	public void print() {
		st.print(0);
	}
	
	public String getTypeString(Type t) {
		/* TO DO */
		String type;
		if (t instanceof IntegerType) {
			type = "int";
		}
		else if (t instanceof IntArrayType) {
			type = "int[]";
		}
		else if (t instanceof BooleanType) {
			type = "boolean";
		}
		else {
			type = t.toString();
		}
		return type;
	}
	
	// MainClass m;
	// ClassDeclList cl;
	public void visit(Program n) {
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}
	}

	// Identifier i1,i2;
	// Statement s;
	public void visit(MainClass n) {
		/* TO DO */
		ClassSymbol mainClassSymbol = new ClassSymbol(n.i1.toString());
		MethodSymbol mainMethodSymbol = new MethodSymbol("main", "void");
		VarSymbol mainVarSymbol = new VarSymbol(n.i2.toString(), "String[]");
		mainMethodSymbol.addParameter(mainVarSymbol);
		mainClassSymbol.addMethod(mainMethodSymbol);
		st.addSymbol(mainClassSymbol);
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		/* TO DO */
		// issue with this is that enterScope returns a symbol table...
		// meaning that each class should be a symbol table
		ClassSymbol classSymbol = new ClassSymbol(n.i.toString());
		st.addSymbol(classSymbol);
		if (n.vl != null) {
			st.enterScope(n.i.toString());
			for (int i = 0; i < n.vl.size(); i++) {
				n.vl.get(i).accept(this);
			}
			st.exitScope();
		}
		if (n.ml != null) {
			st.enterScope(n.i.toString());
			for (int i = 0; i < n.ml.size(); i++) {
				n.ml.get(i).accept(this);
			}
			st.exitScope();
		}
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		/* TO DO */
		ClassSymbol classSymbol = new ClassSymbol(n.i.toString());
		st.addSymbol(classSymbol);
	}

	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
		/* TO DO */
		VarSymbol varSymbol = new VarSymbol(n.i.toString(), getTypeString(n.t));
		st.addSymbol(varSymbol);
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public void visit(MethodDecl n) {
		/* TO DO */
		MethodSymbol methodSymbol = new MethodSymbol(n.i.toString(), getTypeString(n.t));
		st.addSymbol(methodSymbol);
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		/* TO DO */
	}

	// StatementList sl;
	public void visit(Block n) {
		/* TO DO */
	}

	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {
		/* TO DO */
	}

	// Exp e;
	// Statement s;
	public void visit(While n) {
		/* TO DO */
	}

	// Exp e;
	public void visit(Print n) {
	}

	// Identifier i;
	// Exp e;
	public void visit(Assign n) {
	}

	// Identifier i;
	// Exp e1,e2;
	public void visit(ArrayAssign n) {
	}

	// Exp e1,e2;
	public void visit(And n) {
	}

	// Exp e1,e2;
	public void visit(LessThan n) {
	}

	// Exp e1,e2;
	public void visit(Plus n) {
	}

	// Exp e1,e2;
	public void visit(Minus n) {
	}

	// Exp e1,e2;
	public void visit(Times n) {
	}

	// Exp e1,e2;
	public void visit(ArrayLookup n) {
	}

	// Exp e;
	public void visit(ArrayLength n) {
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public void visit(Call n) {
	}

	// int i;
	public void visit(IntegerLiteral n) {
	}

	public void visit(True n) {
	}

	public void visit(False n) {
	}

	public void visit(This n) {
	}

	// Exp e;
	public void visit(NewArray n) {
	}

	// Identifier i = new Identifier();
	public void visit(NewObject n) {
	}

	// Exp e;
	public void visit(Not n) {
	}

	// String s;
	public void visit(IdentifierExp n) {
	}

	// String s;
	public void visit(Identifier n) {
	}
	
	// int[] i;
	public void visit(IntArrayType n) {
	}

	// Bool b;
	public void visit(BooleanType n) {
	}

	// Int i;
	public void visit(IntegerType n) {
	}

	// String s;
	public void visit(IdentifierType n) {
	}	

	// Display added for toy example language. Not used in regular MiniJava
	public void visit(Display n) {
	}
}
