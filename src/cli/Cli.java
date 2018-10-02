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
        final String syntax = "Main";
        final String usageHeader = "MiniJava Compiler";
        final String usageFooter = "";
        formatter.printHelp(syntax, usageHeader, options, usageFooter);
    }
}
