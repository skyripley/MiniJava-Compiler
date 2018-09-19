package cli;

import org.apache.commons.cli.*;

public class Cli {
    private String[] args;
    private Options options = new Options();

    public Cli(String[] args) {
        this.args = args;
        options.addOption("S", "Scan", true, "Scan a source file");
    }

    public String parse() {
        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption("S")) {
                return commandLine.getOptionValue("S");
            }
            else {
                help();
                return null;
            }
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
