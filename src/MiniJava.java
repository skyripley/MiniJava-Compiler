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

    private static int parser(File file) {
        try {
            int return_code = 0;
            ComplexSymbolFactory complexSymbolFactory = new ComplexSymbolFactory();
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream);
            scanner scanner = new scanner(reader, complexSymbolFactory);
            parser parser = new parser(scanner, complexSymbolFactory);
            Symbol root = parser.debug_parse();
            List<Statement> program = (List<Statement>) root.value;
            for (Statement statement : program) {
                statement.accept(new PrettyPrintVisitor());
                System.out.print("\n");
            }
            System.out.print("\nParsing completed");
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
            System.out.println("\n Lexical analysis complete");
            return return_code;
        } catch (Exception exception) {
            System.err.println("Unexpected internal compiler error: " + exception.toString());
            exception.printStackTrace();
            return 1;
        }
    }

    public static void main(String[] args) {
        int return_code = 0;
        Map<String, String> argsMap = new Cli(args).parse();
        if (argsMap != null) {
            if (argsMap.containsKey("S")) {
                String file = argsMap.get("S");
                int scanner_return_code = scanner(new File(file));
            }
            if (argsMap.containsKey("P")) {
                String file = argsMap.get("P");
                int parser_return_code = parser(new File(file));
            }
        }
        System.exit(return_code);
    }
}
