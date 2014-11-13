package compiler.performance;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.utils.TestUtils;

/**
 * This class measures the overall performance including reading the test file, lexing it and parsing the tokens.
 * 
 * @author Andreas Eberle
 */
public class OverallPerformanceMeasurement implements Measurable {
	private static final Path TESTFILE = Paths.get("testdata/parser5/PerformanceGrammar.java");
	private static final int INPUT_REPEATS = 10000;
	private static final int NUMBER_OF_MEASUREMENTS = 15;
	private static final int NUMBER_OF_WARMUPS = 3;
	private Path source;

	public OverallPerformanceMeasurement(Path inputFile, int repeats) throws IOException {
		source = PerformanceUtils.createRepeatedInputFile(inputFile, repeats);
	}

	public static void main(String[] args) throws Exception {
		OverallPerformanceMeasurement measurable = new OverallPerformanceMeasurement(TESTFILE, INPUT_REPEATS);
		DescriptiveStatistics stats = PerformanceUtils.executeMeasurements(measurable, NUMBER_OF_MEASUREMENTS, NUMBER_OF_WARMUPS);

		PerformanceUtils.printStats("overall parsing " + TESTFILE + " (repeated " + INPUT_REPEATS + " times)", stats);
	}

	@Override
	public long measure() throws IOException, ParsingFailedException {
		Parser parser = new Parser(TestUtils.initLexer(source));

		long start = System.currentTimeMillis();
		parser.parse();
		return System.currentTimeMillis() - start;
	}
}