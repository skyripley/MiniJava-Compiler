import java.io.*;
import java.util.HashMap;
import java.util.Map;

import Parser.sym;
import Scanner.*;
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
        int argsLength = args.length;
        Map<String, String> inputArgs = new HashMap<>();
        if (argsLength > 0) {
            for (int i = 0; i < argsLength; i++) {
                switch(args[i]) {
                    case "-S" :
                        System.out.println(i);
                        System.out.println(argsLength - 1);
                        if (i < argsLength - 1) { i++; inputArgs.put("-S", args[i]); }
                }
            }
            if (inputArgs.containsKey("-S")) {
                System.out.println(inputArgs.get("-S"));
                scanner(new File(inputArgs.get("-S")));
            }
        }
        else {
            System.out.println("Please provide input arguments");
        }
    }
}
