package compiler.performance;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.utils.FixedTokensSupplier;
import compiler.utils.TestUtils;

/**
 * This class measures the time required to parse a token test set, which is created by repeating the tokens of a test input file.
 * 
 * @author Andreas Eberle
 */
public class ParserPerformanceMeasurement implements Measurable {
	private static final Path TESTFILE = Paths.get("testdata/parser5/PerformanceGrammar.java");
	private static final int TOKEN_REPEATS = 10000;
	private static final int NUMBER_OF_MEASUREMENTS = 15;
	private static final int NUMBER_OF_WARMUPS = 3;

	private final FixedTokensSupplier tokenSupplier;

	public ParserPerformanceMeasurement(Path path, int tokenRepeats) throws IOException {
		tokenSupplier = initTokenSupplier(path, tokenRepeats);
	}

	public static void main(String[] args) throws Exception {
		ParserPerformanceMeasurement measurable = new ParserPerformanceMeasurement(TESTFILE, TOKEN_REPEATS);
		DescriptiveStatistics stats = PerformanceUtils.executeMeasurements(measurable, NUMBER_OF_MEASUREMENTS, NUMBER_OF_WARMUPS);

		PerformanceUtils.printStats("parsing tokens of " + TESTFILE + " (repeated " + TOKEN_REPEATS + " times)", stats);
	}

	@Override
	public long measure() throws IOException, ParsingFailedException {
		tokenSupplier.reset();
		Parser parser = new Parser(tokenSupplier);

		long start = System.currentTimeMillis();
		parser.parse();
		return System.currentTimeMillis() - start;
	}

	private static FixedTokensSupplier initTokenSupplier(Path path, int tokenRepeats) throws IOException {
		Lexer lexer = TestUtils.initLexer(path);
		ArrayList<Token> tokenList = new ArrayList<Token>();

		Token token;
		do {
			token = lexer.getNextToken();
			tokenList.add(token);
		} while (token.getType() != TokenType.EOF);
		Token eofToken = tokenList.remove(tokenList.size() - 1);
		int numTokens = tokenList.size();

		Token[] tokens = new Token[numTokens * tokenRepeats + 1];
		for (int r = 0; r < tokenRepeats; r++) {
			int idx = r * numTokens;
			for (Token curr : tokenList) {
				tokens[idx] = curr;
				idx++;
			}
		}

		tokens[tokens.length - 1] = eofToken;
		return new FixedTokensSupplier(tokens);
	}
}