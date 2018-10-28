import java.io.*;
import java.util.*;

import AST.symboltable.Table;
import AST.syntaxtree.visitor.backend.X64CodeGenerator;
import AST.syntaxtree.visitor.ops.FunctionDeclaration;
import AST.syntaxtree.visitor.ops.RecordDeclaration;
import AST.syntaxtree.visitor.ops.visitor.IrVisitor;
import Parser.*;
import Parser.sym;
import Scanner.*;
import Symtab.SymbolTable;
import cli.Cli;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import AST.*;
import AST.Visitor.*;
import Symtab.*;
import AST.syntaxtree.visitor.*;
import AST.syntaxtree.*;
import frontend.*;

public class MiniJava {

    private static int generateAssembly(File file) {
        try {
            BuildSymbolTableVisitor symbolTableVisitor = new BuildSymbolTableVisitor();
            int return_code = 0;
            ComplexSymbolFactory complexSymbolFactory = new ComplexSymbolFactory();
            InputStream inputStream = new FileInputStream(file);
            new RamParser(inputStream);
            AST.syntaxtree.Program root = RamParser.Goal();
            BuildSymbolTableVisitor buildSymbolTableVisitor = new BuildSymbolTableVisitor();
            root.accept(buildSymbolTableVisitor);
            TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor(buildSymbolTableVisitor.getSymTab());
            root.accept(typeCheckVisitor);
            if (typeCheckVisitor.getErrorMsg().getHasErrors())
            {
                System.out.println("Error in front-end. Exiting.");
                System.exit(1);
            }
            IrGenerator irGenerator = new IrGenerator(buildSymbolTableVisitor.getSymTab());
            root.accept(irGenerator);
            PrintStream ps = System.out;
            X64CodeGenerator codeGenerator = new X64CodeGenerator(ps);
            for (RecordDeclaration recordDeclaration: irGenerator.getRecordList()) {
                recordDeclaration.accept((IrVisitor) codeGenerator);
            }
            for (FunctionDeclaration functionDeclaration: irGenerator.getFrameList()) {
                functionDeclaration.accept((IrVisitor) codeGenerator);
            }
            return return_code;
        } catch (Exception exception) {
            System.err.println("Unexpected internal compiler error: " + exception.toString());
            exception.printStackTrace();
            return 1;
        }
    }

    private static int generateCode(File file) {
        try {
            int return_code = 0;
            ComplexSymbolFactory complexSymbolFactory = new ComplexSymbolFactory();
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream);
            scanner scanner = new scanner(reader, complexSymbolFactory);
            parser parser = new parser(scanner, complexSymbolFactory);
            Symbol root;
            root = parser.parse();
            if (parser.errorDetected) {
                System.out.println("\nErrors detected during parsing!");
                System.out.println("Will attempt to generate a partial symbol table anyway...");
                return_code = 1;
            }
            AST.Program program = (AST.Program) root.value;
            SymTableVisitor symTableVisitor = new SymTableVisitor();
            symTableVisitor.visit(program);
            SymbolTable symbolTable = symTableVisitor.getSymbolTable();
            TypeCheckingVisitor typeCheckingVisitor = new TypeCheckingVisitor();
            typeCheckingVisitor.setSymtab(symbolTable);
            typeCheckingVisitor.visit(program);
            if (symTableVisitor.getErrors() > 0 || typeCheckingVisitor.getErrors() > 0) {
                return_code = 1;
                System.exit(return_code);
            }
            AST.Visitor.TypeVisitor typeVisitor = new AST.Visitor.TypeVisitor();
            typeVisitor.visit(program);
            CodeTranslateVisitor codeTranslateVisitor = new CodeTranslateVisitor(typeVisitor);
            codeTranslateVisitor.visit(program);
            for (String line: codeTranslateVisitor.getCode()) {
                System.out.println(line);
            }
            return return_code;
        } catch (Exception exception) {
            System.err.println("Unexpected internal compiler error: " + exception.toString());
            exception.printStackTrace();
            return 1;
        }
    }

    private static int semanticAnalyzer(File file) {
        try {
            int return_code = 0;
            ComplexSymbolFactory complexSymbolFactory = new ComplexSymbolFactory();
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream);
            scanner scanner = new scanner(reader, complexSymbolFactory);
            parser parser = new parser(scanner, complexSymbolFactory);
            Symbol root;
            root = parser.parse();
            if (parser.errorDetected) {
                System.out.println("\nErrors detected during parsing!");
                System.out.println("Will attempt to generate a partial symbol table anyway...");
                return_code = 1;
            }
            AST.Program program = (AST.Program) root.value;
            SymTableVisitor symTableVisitor = new SymTableVisitor();
            symTableVisitor.visit(program);
            SymbolTable symbolTable = symTableVisitor.getSymbolTable();
            TypeCheckingVisitor typeCheckingVisitor = new TypeCheckingVisitor();
            typeCheckingVisitor.setSymtab(symbolTable);
            typeCheckingVisitor.visit(program);
            if (symTableVisitor.getErrors() > 0 || typeCheckingVisitor.getErrors() > 0) {
                return_code = 1;
            }
            return return_code;
        } catch (Exception exception) {
            System.err.println("Unexpected internal compiler error: " + exception.toString());
            exception.printStackTrace();
            return 1;
        }
    }

    private static int generateSymbolTable(File file) {
        try {
            int return_code = 0;
            ComplexSymbolFactory complexSymbolFactory = new ComplexSymbolFactory();
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream);
            scanner scanner = new scanner(reader, complexSymbolFactory);
            parser parser = new parser(scanner, complexSymbolFactory);
            Symbol root;
            root = parser.parse();
            if (parser.errorDetected) {
                System.out.println("\nErrors detected during parsing!");
                System.out.println("Will attempt to generate a partial symbol table anyway...");
                return_code = 1;
            }
            AST.Program program = (AST.Program) root.value;
            SymTableVisitor symTableVisitor = new SymTableVisitor();
            symTableVisitor.visit(program);
            symTableVisitor.print();
            return return_code;
        } catch (Exception exception) {
            System.err.println("Unexpected internal compiler error: " + exception.toString());
            exception.printStackTrace();
            return 1;
        }
    }

    private static int parser(File file) {
        try {
            int return_code = 0;
            ComplexSymbolFactory complexSymbolFactory = new ComplexSymbolFactory();
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream);
            scanner scanner = new scanner(reader, complexSymbolFactory);
            parser parser = new parser(scanner, complexSymbolFactory);
            Symbol root;
            root = parser.parse();
            if (parser.errorDetected) {
                System.out.println("\nParsing complete, but syntax errors were detected");
                return_code = 1;
            }
            else {
                System.out.println("Parsing complete - no errors found");
                AST.Program program = (AST.Program) root.value;
                AST.Visitor.PrettyPrintVisitor prettyPrintVisitor = new AST.Visitor.PrettyPrintVisitor();
                prettyPrintVisitor.visit(program);
            }
            return return_code;
        } catch (Exception exception) {
            System.err.println("Unexpected internal compiler error: " + exception.toString());
            exception.printStackTrace();
            return 1;
        }
    }

    private static int scanner(File file) {
        try {
            int return_code = 0;
            ComplexSymbolFactory complexSymbolFactory = new ComplexSymbolFactory();
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream);
            scanner scanner = new scanner(reader, complexSymbolFactory);
            Symbol symbol = scanner.next_token();
            while (symbol.sym != sym.EOF) {
                if (symbol.sym == sym.error) {
                    return_code = 1;
                }
                System.out.print(scanner.symbolToString(symbol) + " ");
                symbol = scanner.next_token();
            }
            System.out.println();
            return return_code;
        } catch (Exception exception) {
            System.err.println("Unexpected internal compiler error: " + exception.toString());
            exception.printStackTrace();
            return 1;
        }
    }

    public static void main(String[] args) {
        int scanner_return_code = 0;
        int parser_return_code = 0;
        int symbol_table_return_code = 0;
        int semantic_analysis_return_code = 0;
        int code_gen_return_code = 0;
        Map<String, String> argsMap = new Cli(args).parse();
        if (argsMap != null) {
            if (argsMap.containsKey("S")) {
                String file = argsMap.get("S");
                scanner_return_code = scanner(new File(file));
            }
            if (argsMap.containsKey("P")) {
                String file = argsMap.get("P");
                parser_return_code = parser(new File(file));
            }
            if (argsMap.containsKey("T")) {
                String file = argsMap.get("T");
                symbol_table_return_code = generateSymbolTable(new File(file));
            }
            if (argsMap.containsKey("A")) {
                String file = argsMap.get("A");
                semantic_analysis_return_code = semanticAnalyzer(new File(file));
            }
            if (argsMap.containsKey("C")) {
                String file = argsMap.get("C");
                code_gen_return_code = generateAssembly(new File(file));
            }
        }
        if (scanner_return_code == 1 || parser_return_code == 1 || symbol_table_return_code == 1
                || semantic_analysis_return_code == 1 || code_gen_return_code == 1) {
            System.exit(1);
        }
        else {
            System.exit(0);
        }
    }
}
