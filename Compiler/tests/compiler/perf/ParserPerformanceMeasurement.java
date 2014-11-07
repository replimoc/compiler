package compiler.perf;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenSuppliable;
import compiler.lexer.TokenType;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import compiler.utils.FixedTokensSupplier;
import compiler.utils.TestUtils;

/**
 * TODO document me
 */
public class ParserPerformanceMeasurement {

	public static long measureParser(TokenSuppliable tokenSupplier) throws IOException, ParserException {
		Parser parser = new Parser(tokenSupplier);

		long startTime = System.currentTimeMillis();
		parser.parse();
		return System.currentTimeMillis() - startTime;
	}

	public static void main(String[] args) throws IOException, ParserException {
		Path path = Paths.get("testdata/parser5/CorrectGrammar1.java");
		int tokenRepeats = 10000;
		int numMeasures = 20;

		DescriptiveStatistics stats = new DescriptiveStatistics();
		FixedTokensSupplier tokenSupplier = initTokenSupplier(path, tokenRepeats);

		// warm up system to reduce variations, which are enormous in first runs.
		for (int j = 0; j < 5; j++)
		{
			tokenSupplier.reset();
			measureParser(tokenSupplier);
		}

		// execute actuall measurement
		for (int j = 0; j < numMeasures; j++)
		{
			tokenSupplier.reset();
			long measuredTime = measureParser(tokenSupplier);
			stats.addValue(measuredTime);
		}

		System.out.println("results for " + path + "(repeat " + tokenRepeats + " times)");
		System.out.printf("\t mean(ms) = %.3f\n", stats.getMean());
		System.out.println("\t  min(ms) = " + stats.getMin());
		System.out.printf("\tstdev(ms) = %.3f\n", stats.getStandardDeviation());
		System.out.println("\t  max(ms) = " + stats.getMax());
		System.out.printf("\t stdev(%%) = %.2f\n", stats.getStandardDeviation() / stats.getMean() * 100);

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