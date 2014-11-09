package compiler.performance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class IOPerformanceMeasurement implements Measurable {

	// private static final Path TESTFILE = Paths.get("./testdata/AllTokens.java");
	// private static final int INPUT_FILE_REPEATS = 30000;

	private static final Path TESTFILE = Paths.get("testdata/parser5/PerformanceGrammar.java");
	private static final int INPUT_FILE_REPEATS = 10000;

	private static final int NUMBER_OF_MEASUREMENTS = 20;
	private static final int NUMBER_OF_WARUMUPS = 10;

	private Path testFile;

	public IOPerformanceMeasurement(Path path, int numberOfInputRepeats) throws IOException {
		testFile = Files.createTempFile("io-speedtest", ".tmp");

		createRepeatedInputFile(path, numberOfInputRepeats, testFile);
	}

	public static void main(String args[]) throws Exception {
		IOPerformanceMeasurement measurable = new IOPerformanceMeasurement(TESTFILE, INPUT_FILE_REPEATS);
		DescriptiveStatistics stats = PerformanceUtils.executeMeasurements(measurable, NUMBER_OF_MEASUREMENTS, NUMBER_OF_WARUMUPS);

		PerformanceUtils.printStats("iotest " + TESTFILE + " (repeated " + INPUT_FILE_REPEATS + " times)", stats);
	}

	private static void createRepeatedInputFile(Path inputFile, int numberOfInputRepeats, Path outputFile) throws IOException {
		byte[] inputBytes = Files.readAllBytes(inputFile);

		OutputStream out = Files.newOutputStream(outputFile);
		for (int i = 0; i < numberOfInputRepeats; i++) {
			out.write(inputBytes);
		}
		out.close();
	}

	@Override
	public long measure() throws Exception {
		BufferedReader reader = Files.newBufferedReader(testFile, StandardCharsets.US_ASCII);

		long start = System.currentTimeMillis();

		int character;
		do {
			character = reader.read();
		} while (character != -1);

		return System.currentTimeMillis() - start;
	}
}
