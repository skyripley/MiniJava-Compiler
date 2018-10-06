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
			IdentifierType identifierType = (IdentifierType) t;
			type = identifierType.s;
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
		/* instantiate MethodDecl for main and add its sym table */
		SymbolTable symbolTable = new SymbolTable();
		ClassSymbol mainClassSymbol = new ClassSymbol(n.i1.toString());
		st.addSymbol(mainClassSymbol);
		st.addChild(n.toString(), symbolTable);
		st = st.enterScope(n.i1.toString());
		MethodSymbol mainMethodSymbol = new MethodSymbol("main", "void");
		VarSymbol mainVarSymbol = new VarSymbol(n.i2.toString(), "String[]");
		mainMethodSymbol.addParameter(mainVarSymbol);
		mainClassSymbol.addMethod(mainMethodSymbol);
		st = st.exitScope();
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		/* TO DO */
		SymbolTable symbolTable = new SymbolTable();
		ClassSymbol classSymbol = new ClassSymbol(n.i.toString());
		st.addSymbol(classSymbol);
		st.addChild(n.toString(), symbolTable);
		st = st.enterScope(n.i.toString());
		if (n.vl != null) {
			for (int i = 0; i < n.vl.size(); i++) {
				VarSymbol varSymbol = new VarSymbol(n.vl.get(i).i.toString(), getTypeString(n.vl.get(i).t));
				classSymbol.addVariable(varSymbol);
				n.vl.get(i).accept(this);
			}
		}
		if (n.ml != null) {
			for (int i = 0; i < n.ml.size(); i++) {
				MethodSymbol methodSymbol = new MethodSymbol(n.ml.get(i).i.toString(), getTypeString(n.ml.get(i).t));
				if (n.ml.get(i).fl != null) {
					for (int j = 0; j < n.ml.get(i).fl.size(); j++) {
						Formal formal = n.ml.get(i).fl.get(j);
						VarSymbol varSymbol = new VarSymbol(formal.i.toString(), getTypeString(formal.t));
						methodSymbol.addParameter(varSymbol);
					}
				}
				classSymbol.addMethod(methodSymbol);
				n.ml.get(i).accept(this);
			}
		}
		st = st.exitScope();
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
		SymbolTable symbolTable = new SymbolTable();
		MethodSymbol methodSymbol = new MethodSymbol(n.i.toString(), getTypeString(n.t));
		st.addSymbol(methodSymbol);
		st.addChild(n.toString(), symbolTable);
		st = st.enterScope(n.i.toString());
		if (n.fl != null) {
			for (int i = 0; i < n.fl.size(); i++) {
				VarSymbol varSymbol = new VarSymbol(n.fl.get(i).i.toString(), getTypeString(n.fl.get(i).t));
				methodSymbol.addParameter(varSymbol);
				n.fl.get(i).accept(this);
			}
		}
		if (n.vl != null) {
			for (int i = 0; i < n.vl.size(); i++) {
				n.vl.get(i).accept(this);
			}
		}
		st = st.exitScope();
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		/* TO DO */
		VarSymbol formal = new VarSymbol(n.i.toString(), getTypeString(n.t));
		st.addSymbol(formal);
	}

	// StatementList sl;
	public void visit(Block n) {
		/* TO DO */
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
	}

	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {
		/* TO DO */
		n.e.accept(this);
		n.s1.accept(this);
		n.s2.accept(this);
	}

	// Exp e;
	// Statement s;
	public void visit(While n) {
		/* TO DO */
		n.e.accept(this);
		n.s.accept(this);
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
