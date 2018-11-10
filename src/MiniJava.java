import Scanner.*;
import Parser.parser;
import Parser.sym;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import java.io.*;
import java.util.List;

import AST.*;
import AST.Program;
//import AST.Statement;
import AST.Visitor.PrettyPrintVisitor;
import AST.Visitor.SemanticAnalysisVisitor;
import AST.Visitor.SymTableVisitor;
import AST.Visitor.CodeTranslateVisitor;

public class MiniJava {
	
    public static void main(String [] args) 
    {
    	int errors = 0;
		String help = "Use:\n" +
				  	  "  java MiniJava -S <source_file>\n" +
				  	  "  java MiniJava -P <source_file>\n" +
				  	  "  java MiniJava -T <source_file>\n" +
				  	  "  java MiniJava -A <source_file>\n" +
				  	  "  java MiniJava -C <source_file>\n";
    	
    	if ( args.length == 1 && (args[0].equals("-h") || args[0].equals("-H")) )
    	{
        	// print the help
        	System.out.println(help);
    	}
    	else if ( args.length == 2 && args[0].equals("-S") )
    	{
        	// run scanner on the source file
    		errors = FileScanner( args[1] );
    	}
    	else if ( args.length == 2 && args[0].equals("-P") )
    	{
        	// run the parser on the source file
    		errors = FileParser( args[1] );
    	}
    	else if ( args.length == 2 && args[0].equals("-T") )
    	{
        	// run the parser & semantic analysis on the source file
    		errors = FileSymtab( args[1] );
    	}
    	else if ( args.length == 2 && args[0].equals("-A") )
    	{
        	// run the parser & semantic analysis on the source file
    		errors = FileSemantics( args[1] );
    	}
    	else if ( args.length == 2 && args[0].equals("-C") )
    	{
        	// run the translation on the source file
    		errors = FileTranslate( args[1] );
    	}
    	else
    	{
    		System.err.println("Invalid program arguments.\n" + help);
    		errors = 1;
    	}
    	
    	if ( args.length > 1 && args[0].equals("-C") && errors > 0 ) {
    		System.out.println(errors + " errors were found.");
    	}
    	
		System.exit( errors == 0 ? 0 : 1 );
   }

	public static int FileScanner(String source_file)
	{
		int errors = 0;
		
	    try {
	        // create a scanner on the input file
	        ComplexSymbolFactory sf = new ComplexSymbolFactory();
	        Reader in = new BufferedReader(new FileReader(source_file));
	        scanner s = new scanner(in, sf);
	        Symbol t = s.next_token();
	        while (t.sym != sym.EOF){ 
	        	if ( t.sym == sym.error ) ++errors;
	        	
	            // print each token that we scanned
        		System.out.print(s.symbolToString(t) + "\n");
	            t = s.next_token();
	        }
	        
	        System.out.println("\nLexical analysis completed"); 
	    
	    } catch (Exception e) {
	        // yuck: some kind of error in the compiler implementation
	        // that we're not expecting (a bug!)
	        System.err.println("Unexpected internal compiler error: " + 
	                    e.toString());
	        // print out a stack dump
	        e.printStackTrace();
	    }
	    
	    return errors;    	
	}

	public static int FileParser(String source_file)
	{
		int errors = 0;
		
	    try {
	        // create a parser on the input file
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
	        Reader in = new BufferedReader(new FileReader(source_file));
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
		    // replace p.parse() with p.debug_parse() in next line to see trace of
		    // parser shift/reduce actions during parse
            root = p.parse();  
            Program program = (Program)root.value;
            program.accept( new PrettyPrintVisitor() );
	    
	        System.out.println("\nParsing completed"); 
	    } catch (Exception e) {
	        // yuck: some kind of error in the compiler implementation
	        // that we're not expecting (a bug!)
	        System.err.println("Unexpected internal compiler error: " + 
	                    e.toString());
	        // print out a stack dump
	        e.printStackTrace();
	    }
	    
	    return errors;    	
	}
	
	public static int FileSymtab(String source_file)
	{
		int errors = 0;
		
	    try {
	        // create a parser on the input file
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
	        Reader in = new BufferedReader(new FileReader(source_file));
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
		    // replace p.parse() with p.debug_parse() in next line to see trace of
		    // parser shift/reduce actions during parse
            root = p.parse();  
            Program program = (Program)root.value;
            //program.accept( new PrettyPrintVisitor() );
            if ( errors > 0 ) return errors;
            
            SymTableVisitor st = new SymTableVisitor();
            program.accept( st );
            st.print();
	    
	        System.out.println("\nSymbol table generation completed"); 
	    } catch (Exception e) {
	        // yuck: some kind of error in the compiler implementation
	        // that we're not expecting (a bug!)
	        System.err.println("Unexpected internal compiler error: " + 
	                    e.toString());
	        // print out a stack dump
	        e.printStackTrace();
	    }
	    
	    return errors;
	}	
	
	public static int FileSemantics(String source_file)
	{
		int errors = 0;
		
	    try {
	        // create a parser on the input file
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
	        Reader in = new BufferedReader(new FileReader(source_file));
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
		    // replace p.parse() with p.debug_parse() in next line to see trace of
		    // parser shift/reduce actions during parse
            root = p.parse();  
            Program program = (Program)root.value;
            //program.accept( new PrettyPrintVisitor() );
            if ( errors > 0 ) return errors;
            
            SymTableVisitor st = new SymTableVisitor();
            program.accept( st );
            //st.print();
            errors += st.errors;
            //if ( errors > 0 ) return errors;

            SemanticAnalysisVisitor sa = new SemanticAnalysisVisitor();
            sa.setSymtab(st.getSymtab());
            program.accept( sa );
            errors += sa.errors;
            
	        System.out.println("\nSemantic analysis completed"); 
	    } catch (Exception e) {
	        // yuck: some kind of error in the compiler implementation
	        // that we're not expecting (a bug!)
	        System.err.println("Unexpected internal compiler error: " + 
	                    e.toString());
	        // print out a stack dump
	        e.printStackTrace();
	    }
	    
	    return errors;
	}	

	public static int FileTranslate(String source_file)
	{
		int errors = 0;
		
	    try {
	        // create a parser on the input file
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
	        Reader in = new BufferedReader(new FileReader(source_file));
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
		    // replace p.parse() with p.debug_parse() in next line to see trace of
		    // parser shift/reduce actions during parse
            root = p.parse();  
            Program program = (Program)root.value;
            //program.accept( new PrettyPrintVisitor() );
            if ( errors > 0 ) return errors;
            
            SymTableVisitor st = new SymTableVisitor();
            program.accept( st );
            //st.print();
            errors += st.errors;
        	if ( errors > 0 ) return errors;
            
            SemanticAnalysisVisitor sa = new SemanticAnalysisVisitor();
            sa.setSymtab(st.getSymtab());
            program.accept( sa );
            errors += sa.errors;
            if ( errors > 0 ) return errors;
            
            CodeTranslateVisitor ct = new CodeTranslateVisitor();
            ct.setSymtab(st.getSymtab());
            program.accept( ct );
            errors += ct.errors;
            if ( errors > 0 ) return errors;
            
	        //System.out.println("\nCode translation completed"); 

	    } catch (Exception e) {
	        // yuck: some kind of error in the compiler implementation
	        // that we're not expecting (a bug!)
	        System.err.println("Unexpected internal compiler error: " + 
	                    e.toString());
	        // print out a stack dump
	        e.printStackTrace();
	    }
	    
	    return errors;    	
	}	
}



