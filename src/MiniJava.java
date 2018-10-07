import java.io.*;
import java.util.*;
import Parser.*;
import Parser.sym;
import Scanner.*;
import cli.Cli;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import AST.*;
import AST.Visitor.*;

public class MiniJava {

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
            Program program = (Program) root.value;
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
                Program program = (Program) root.value;
                PrettyPrintVisitor prettyPrintVisitor = new PrettyPrintVisitor();
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
        }
        if (scanner_return_code == 1 || parser_return_code == 1 || symbol_table_return_code == 1) {
            System.exit(1);
        }
        else {
            System.exit(0);
        }
    }
}
