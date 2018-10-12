package cli;

import java.util.Map;
import java.util.HashMap;
import org.apache.commons.cli.*;

public class Cli {
    private String[] args;
    private Options options = new Options();

    public Cli(String[] args) {
        this.args = args;
        options.addOption("S", "Scan", true, "Scan a source file");
        options.addOption("P", "Parse", true, "Parse a source file");
        options.addOption("T", "SymbolTable", true, "Generate a symbol table from a source file");
        options.addOption("A", "SemanticAnalysis", true, "Perform semantic analysis on a source file");
    }

    public Map<String, String> parse() {
        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = null;
        Map<String, String> argsMap = new HashMap<>();
        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption("S")) {
                argsMap.put("S", commandLine.getOptionValue("S"));
            }
            if (commandLine.hasOption("P")) {
                argsMap.put("P", commandLine.getOptionValue("P"));
            }
            if (commandLine.hasOption("T")) {
                argsMap.put("T", commandLine.getOptionValue("T"));
            }
            if (commandLine.hasOption("A")) {
                argsMap.put("A", commandLine.getOptionValue("A"));
            }
            if (argsMap.isEmpty()) {
                help();
                return null;
            }
            return argsMap;
        } catch (ParseException ex) {
            help();
            return null;
        }
    }

    private void help() {
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = "java MiniJava [-S <file_path>] [-P <file_path>]";
        final String usageHeader = "Scan or parse an input file\n\n";
        final String usageFooter = "";
        formatter.printHelp(syntax, usageHeader, options, usageFooter);
    }
}
