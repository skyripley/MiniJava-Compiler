package AST.Visitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import AST.*;
import Symtab.*;


public class SymTableVisitor implements Visitor {

	private SymbolTable st = new SymbolTable();
	
	public void print() {
		st.print(0);
	}
	
	private String getTypeString(Type t) {
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

	private void visitFormalList(FormalList formalList, MethodSymbol methodSymbol, boolean acceptVisitor) {
		for (int i = 0; i < formalList.size(); i++) {
			Formal formal = formalList.get(i);
			VarSymbol varSymbol = new VarSymbol(formal.i.toString(), getTypeString(formal.t));
			methodSymbol.addParameter(varSymbol);
			if (acceptVisitor) {
				formalList.get(i).accept(this);
			}
		}
	}

	private void visitMethodDeclList(MethodDeclList methodDeclList, ClassSymbol classSymbol) {
		for (int i = 0; i < methodDeclList.size(); i++) {
			MethodSymbol methodSymbol = new MethodSymbol(methodDeclList.get(i).i.toString(),
					getTypeString(methodDeclList.get(i).t));
			if (methodDeclList.get(i).fl != null) {
				visitFormalList(methodDeclList.get(i).fl, methodSymbol, false);
			}
			classSymbol.addMethod(methodSymbol);
			methodDeclList.get(i).accept(this);
		}
	}

	private void visitVarDeclList(VarDeclList varDeclList, boolean addSymbol, ClassSymbol classSymbol) {
		for (int i = 0; i < varDeclList.size(); i++) {
			if (addSymbol) {
				VarSymbol varSymbol = new VarSymbol(varDeclList.get(i).i.toString(), getTypeString(varDeclList.get(i).t));
				classSymbol.addVariable(varSymbol);
				st.addSymbol(varSymbol);
			}
			varDeclList.get(i).accept(this);
		}
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
		SymbolTable symbolTable = new SymbolTable();
		ClassSymbol mainClassSymbol = new ClassSymbol(n.i1.toString());
		st.addSymbol(mainClassSymbol);
		st.addChild(n.toString(), symbolTable);
		st = st.enterScope(n.i1.toString());
		MethodSymbol mainMethodSymbol = new MethodSymbol("main", "void");
		VarSymbol mainVarSymbol = new VarSymbol(n.i2.toString(), "String[]");
		mainMethodSymbol.addParameter(mainVarSymbol);
		mainClassSymbol.addMethod(mainMethodSymbol);
		SymbolTable mainSymbolTable = new SymbolTable();
		st.addSymbol(mainMethodSymbol);
		st.addChild("main", mainSymbolTable);
		st = st.enterScope("main");
		st.addSymbol(mainVarSymbol);
		st = st.exitScope();
		st = st.exitScope();
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		SymbolTable symbolTable = new SymbolTable();
		ClassSymbol classSymbol = new ClassSymbol(n.i.toString());
		st.addSymbol(classSymbol);
		st.addChild(n.toString(), symbolTable);
		st = st.enterScope(n.i.toString());
		if (n.vl != null) {
			visitVarDeclList(n.vl, true, classSymbol);
		}
		if (n.ml != null) {
			visitMethodDeclList(n.ml, classSymbol);
		}
		st = st.exitScope();
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		SymbolTable symbolTable = new SymbolTable();
		ClassSymbol classSymbol = new ClassSymbol(n.i.toString());
		st.addSymbol(classSymbol);
		st.addChild(n.toString(), symbolTable);
		st = st.enterScope(n.i.toString());
		if (n.vl != null) {
			visitVarDeclList(n.vl, true, classSymbol);
		}
		if (n.ml != null) {
			visitMethodDeclList(n.ml, classSymbol);
		}
		st = st.exitScope();
	}

	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
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
		SymbolTable symbolTable = new SymbolTable();
		MethodSymbol methodSymbol = new MethodSymbol(n.i.toString(), getTypeString(n.t));
		st.addSymbol(methodSymbol);
		st.addChild(n.toString(), symbolTable);
		st = st.enterScope(n.i.toString());
		if (n.fl != null) {
			visitFormalList(n.fl, methodSymbol, true);
		}
		if (n.vl != null) {
			visitVarDeclList(n.vl, false, null);
		}
		st = st.exitScope();
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		VarSymbol formal = new VarSymbol(n.i.toString(), getTypeString(n.t));
		st.addSymbol(formal);
	}

	// StatementList sl;
	public void visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
	}

	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {
		n.e.accept(this);
		n.s1.accept(this);
		n.s2.accept(this);
	}

	// Exp e;
	// Statement s;
	public void visit(While n) {
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
