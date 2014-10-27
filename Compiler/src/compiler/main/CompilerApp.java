package compiler.main;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CompilerApp {

	public static void main(String args[]) {
		Options options = new Options();
		Option help = new Option("help", "print this message");
		options.addOption(help);

		CommandLineParser parser = new GnuParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("help"))
			{
				printHelp(options);
				return;
			}
		} catch (ParseException e) {
			System.err.println("Wrong Command Line Parameters: " + e.getMessage());
		}
		printHelp(options);
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("compiler", options);
	}
}
