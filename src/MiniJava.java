import java.io.*;

import Parser.sym;
import Scanner.*;
import cli.Cli;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;

public class MiniJava {

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
        String file = new Cli(args).parse();
        if (file != null) {
            return_code = scanner(new File(file));
        }
        System.exit(return_code);
    }
}
