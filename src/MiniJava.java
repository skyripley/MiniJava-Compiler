import java.io.*;

import Parser.sym;
import Scanner.*;
import cli.Cli;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;

public class MiniJava {

    private static void scanner(File file) {
        try {
            ComplexSymbolFactory complexSymbolFactory = new ComplexSymbolFactory();
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream);
            scanner scanner = new scanner(reader, complexSymbolFactory);
            Symbol symbol = scanner.next_token();
            while (symbol.sym != sym.EOF) {
                System.out.print(scanner.symbolToString(symbol) + " ");
                symbol = scanner.next_token();
            }
            System.out.println("\n Lexical analysis complete");
            System.exit(0);
        } catch (Exception exception) {
            System.err.println("Unexpected internal compiler error: " + exception.toString());
            exception.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        String file = new Cli(args).parse();
        if (file != null) {
            scanner(new File(file));
        }
    }
}
