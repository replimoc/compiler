package compiler.main;

import compiler.MiniJavaCompiler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.List;

public class CompilerApp {

	public static void main(String args[]) {
		Options options = new Options();
        options.addOption( "h", "help", false, "print this message" );
        options.addOption( "l", "lextest", false, "write output of Lexer to stdout" );
//        Example of option with argument
//        options.addOption( OptionBuilder.withLongOpt( "block-size" )
//                .withDescription( "use SIZE-byte blocks" )
//                .hasArg()
//                .withArgName("SIZE")
//                .create() );

        CommandLineParser parser = new GnuParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("help"))
			{
				printHelp(options);
				System.exit(0);
			}

            if (cmd.hasOption("lextest"))
            {
                MiniJavaCompiler.setOutputFormat(MiniJavaCompiler.OutputFormat.LEXER);
            }

            List args1 = cmd.getArgList();
            if (args1.size() != 1) {
                System.err.println( "Missing Filename / To Many Arguments");
                System.exit(-1);
            }

            MiniJavaCompiler.compile((String) args1.get(0));

        } catch (ParseException e) {
            System.err.println("Wrong Command Line Parameters: " + e.getMessage());
            System.exit(-1);
        }
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("compiler", options);
	}
}
