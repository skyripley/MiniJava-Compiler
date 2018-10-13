package AST.Visitor;

import java.util.HashMap;
import java.util.Iterator;

import AST.*;
import Symtab.*;


public class SymTableVisitor implements Visitor {

	SymbolTable st = new SymbolTable();

	public void print()
	{
		st.print(0);
	}

	public String getTypeString(Type t) {
		if ( t == null )
			return "";
		else if ( t instanceof IntArrayType ) {
			return "int[]";
		}
		else if ( t instanceof BooleanType ) {
			return "boolean";
		}
		else if ( t instanceof IntegerType ) {
			return "int";
		}
		else if ( t instanceof IdentifierType ) {
			return ((IdentifierType)t).s;
		}
		else return "";
	}

	public SymbolTable getSymbolTable() {
		return st;
	}

	// Display added for toy example language. Not used in regular MiniJava
	public void visit(Display n) {
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
		ClassSymbol c = new ClassSymbol(n.i1.toString(), "");
		st.addSymbol(c);
		st = st.enterScope(n.i1.toString());
		MethodSymbol s = new MethodSymbol("main", "void");
		s.addParameter(new VarSymbol(n.i2.toString(), "String[]"));
		st.addSymbol(s);
		st = st.enterScope("main");
		st.addSymbol(new VarSymbol(n.i2.toString(), "String[]"));
		n.s.accept(this);
		st = st.exitScope();

		for ( Iterator<String> i = st.getMethodTable().keySet().iterator(); i.hasNext(); ) {
			String id = (String)i.next();
			Symbol sym = st.getMethodTable().get(id);
			if ( sym instanceof MethodSymbol ) {
				c.addMethod((MethodSymbol)sym);
			}
		}

		for ( Iterator<String> i = st.getVarTable().keySet().iterator(); i.hasNext(); ) {
			String id = (String)i.next();
			Symbol sym = st.getVarTable().get(id);
			if ( sym instanceof VarSymbol ) {
				c.addVariable((VarSymbol)sym);
			}
		}

		st = st.exitScope();
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		ClassSymbol c = new ClassSymbol(n.i.toString());
		st.addSymbol(c);
		st = st.enterScope(n.i.toString());
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
		for ( Iterator<String> i = st.getMethodTable().keySet().iterator(); i.hasNext(); ) {
			String id = (String)i.next();
			Symbol sym = st.getMethodTable().get(id);
			if ( sym instanceof MethodSymbol ) {
				c.addMethod((MethodSymbol)sym);
			}
		}

		for ( Iterator<String> i = st.getVarTable().keySet().iterator(); i.hasNext(); ) {
			String id = (String)i.next();
			Symbol sym = st.getVarTable().get(id);
			if ( sym instanceof VarSymbol ) {
				c.addVariable((VarSymbol)sym);
			}
		}

		st = st.exitScope();
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		ClassSymbol c = new ClassSymbol(n.i.toString(), n.j.toString());
		st.addSymbol(c);

		// search for the extend class and add to the symbol
		SymbolTable ext_st = st.getChild(n.j.toString());
		Symbol s = ext_st.lookupSymbol(n.j.toString());
		if ( s != null && s instanceof ClassSymbol ) {
			c.extendsClass((ClassSymbol)s);
		}

		// enter a new scope
		st = st.enterScope(n.i.toString());

		// add variables & methods
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
		// add the variables and declarations from the symbol table to the class symbol
		for ( Iterator<String> i = st.getMethodTable().keySet().iterator(); i.hasNext(); ) {
			String id = (String)i.next();
			Symbol sym = st.getMethodTable().get(id);
			if ( sym instanceof MethodSymbol ) {
				c.addMethod((MethodSymbol)sym);
			}
		}

		for ( Iterator<String> i = st.getVarTable().keySet().iterator(); i.hasNext(); ) {
			String id = (String)i.next();
			Symbol sym = st.getVarTable().get(id);
			if ( sym instanceof VarSymbol ) {
				c.addVariable((VarSymbol)sym);
			}
		}

		st = st.exitScope();
	}

	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
		st.addSymbol(new VarSymbol(n.i.toString(), getTypeString(n.t)));
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public void visit(MethodDecl n) {
		MethodSymbol s = new MethodSymbol(n.i.toString(), getTypeString(n.t));
		if (n.fl != null) {
			for (int i = 0; i < n.fl.size(); i++) {
				Formal f = n.fl.get(i);
				if (f != null) {
					s.addParameter(new VarSymbol(f.i.toString(), getTypeString(f.t)));
				}
			}
		}
		st.addSymbol(s);
		st = st.enterScope(n.i.toString());
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
		st = st.exitScope();
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		st.addSymbol(new VarSymbol(n.i.toString(), getTypeString(n.t)));
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

	// StatementList sl;
	public void visit(Block n) {
		st = st.enterScope("block");
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
		st = st.exitScope();
	}

	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {
		n.s1.accept(this);
		n.s2.accept(this);
	}

	// Exp e;
	// Statement s;
	public void visit(While n) {
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
}
