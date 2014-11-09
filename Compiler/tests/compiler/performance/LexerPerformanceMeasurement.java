package compiler.performance;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import compiler.StringTable;
import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;

public class LexerPerformanceMeasurement implements Measurable {

	private static final int INPUT_FILE_REPEATS = 30000;
	private static final int NUMBER_OF_MEASUREMENTS = 20;
	private static final int NUMBER_OF_WARUMUPS = 10;

	private final String toLex;

	public LexerPerformanceMeasurement(Path path, int numberOfInputRepeats) throws IOException {
		this.toLex = repeatInput(path, numberOfInputRepeats);
	}

	public static void main(String args[]) throws Exception {
		Path path = Paths.get("./testdata/AllTokens.java");

		LexerPerformanceMeasurement measurable = new LexerPerformanceMeasurement(path, INPUT_FILE_REPEATS);
		DescriptiveStatistics stats = PerformanceUtils.executeMeasurements(measurable, NUMBER_OF_MEASUREMENTS, NUMBER_OF_WARUMUPS);

		PerformanceUtils.printStats("lexing " + path + " (repeated " + INPUT_FILE_REPEATS + " times)", stats);
	}

	private static String repeatInput(Path path, int numberOfInputRepeats) throws IOException {
		byte[] encoded = Files.readAllBytes(path);
		String fileContent = new String(encoded, "US-ASCII");

		StringBuffer buffer = new StringBuffer(fileContent.length() * numberOfInputRepeats);
		for (int i = 0; i < numberOfInputRepeats; i++) {
			buffer.append(fileContent);
		}
		return buffer.toString();
	}

	@Override
	public long measure() throws Exception {
		StringReader reader = new StringReader(toLex);
		Lexer lexer = new Lexer(reader, new StringTable());

		long start = System.currentTimeMillis();

		Token token;
		do {
			token = lexer.getNextToken();
		} while (token.getType() != TokenType.EOF);

		return System.currentTimeMillis() - start;
	}
}
