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
import compiler.parser.ParserException;
import compiler.utils.FixedTokensSupplier;
import compiler.utils.TestUtils;

/**
 * TODO document me
 */
public class ParserPerformanceMeasurement implements Measurable {
	private static final int TOKEN_REPEATS = 10000;
	private static final int NUMBER_OF_MEASUREMENTS = 20;
	private static final int NUMBER_OF_WARMUPS = 10;

	private final FixedTokensSupplier tokenSupplier;

	public ParserPerformanceMeasurement(Path path, int tokenRepeats) throws IOException {
		tokenSupplier = initTokenSupplier(path, tokenRepeats);
	}

	public static void main(String[] args) throws Exception {
		final Path path = Paths.get("testdata/parser5/PerformanceGrammar.java");

		ParserPerformanceMeasurement measurable = new ParserPerformanceMeasurement(path, TOKEN_REPEATS);
		DescriptiveStatistics stats = PerformanceUtils.executeMeasurements(measurable, NUMBER_OF_MEASUREMENTS, NUMBER_OF_WARMUPS);

		PerformanceUtils.printStats(path + " (repeated " + TOKEN_REPEATS + " times)", stats);
	}

	@Override
	public long measure() throws IOException, ParserException {
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