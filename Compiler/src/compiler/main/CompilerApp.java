package compiler.main;

import compiler.CompilerPipeline;
import org.apache.commons.cli.*;

import java.util.List;

public class CompilerApp {

	public static void main(String args[]) {

        CompilerPipeline compiler = new CompilerPipeline();

        Options options = new Options();
        Option help = new Option( "help", "print this message" );
        Option debug = new Option( "debug", "print debugging information" );
        options.addOption(help);
        options.addOption(debug);

        CommandLineParser parser = new BasicParser(); // TODO what style of options to use
        try {
            CommandLine cmd = parser.parse( options, args);
            if (cmd.hasOption("help"))
            {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "compiler", options );
                System.exit(0);
            }

            if (cmd.hasOption("debug"))
            {
                compiler.setDebugOn();
            }

            List args1 = cmd.getArgList();

            if (args1.size() != 1) {
                System.err.println( "Missing Filename / To Many Arguments");
                System.exit(1);
            }

            compiler.setSourceFileName((String) args1.get(0));

        } catch (ParseException e) {
            System.err.println( "Wrong Command Line Parameters: " + e.getMessage() );
            System.exit(1);
        }

        try {
            compiler.compile();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
