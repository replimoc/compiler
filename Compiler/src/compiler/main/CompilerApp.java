package compiler.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import compiler.StringTable;
import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;
import compiler.parser.Parser;

public class CompilerApp {

	private static final String LEXTEST = "lextest";

	public static void main(String args[]) {
		int exitCode = execute(args);
		System.exit(exitCode);
	}

	private static int execute(String[] args) {
		Options options = new Options();
		options.addOption("h", "help", false, "print this message");
		options.addOption(null, LEXTEST, true, "print tokens generated by lexer");

		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("help"))
			{
				printHelp(options);
				return 0;
			}

			if (cmd.hasOption(LEXTEST)) {
				File file = new File(cmd.getOptionValue(LEXTEST));
				try {
					if (file.exists()) {
						executeLexerTest(file);
						return 0;
					} else {
						System.err.println("File " + file + " does not exist.");
					}
				} catch (IOException e) {
					System.err.println("Cannot access file " + file);
				}
			}

			String[] remainingArgs = cmd.getArgs();
			if (remainingArgs.length == 1) {
				File file = new File(remainingArgs[0]);
				try {
					if (file.exists()) {
						return executeParsing(file);
					} else {
						System.err.println("File " + file + " does not exist.");
					}
				} catch (IOException e) {
					System.err.println("Cannot access file " + file);
				}
			}
		} catch (ParseException e) {
			System.err.println("Wrong Command Line Parameters: " + e.getMessage());
		} catch (Throwable t) {
			System.err.println("Unexpected exception occured: " + t.getMessage());
		}
		printHelp(options);
		return 1;
	}

	private static int executeParsing(File file) throws IOException {
		Lexer lexer = new Lexer(new BufferedReader(new FileReader(file)), new StringTable());
		Parser parser = new Parser(lexer);

		// if successful return 0
		if (parser.parse() == 0)
			return 0;
		return 1;
	}

	private static void executeLexerTest(File file) throws IOException {
		Lexer lexer = new Lexer(new BufferedReader(new FileReader(file)), new StringTable());

		Token token;
		do {
			token = lexer.getNextToken();
			System.out.println(token.getTokenString());
		} while (token.getType() != TokenType.EOF);
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("compiler [options] [file]", options);
	}
}