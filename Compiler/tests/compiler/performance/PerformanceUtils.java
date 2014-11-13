package compiler.performance;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public final class PerformanceUtils {
	private PerformanceUtils() {
	}

	public static DescriptiveStatistics executeMeasurements(Measurable measurable, final int numMeasures, final int numWarmup) throws Exception {
		System.out.println("Starting performance measurement with " + numWarmup + " warmups and " + numMeasures + " measurements.");

		DescriptiveStatistics stats = new DescriptiveStatistics();

		final int numRuns = numMeasures + numWarmup;

		for (int i = 0; i < numRuns; i++)
		{
			Thread.sleep(1000);

			final boolean warmup = i < numWarmup;

			final long measuredTime = measurable.measure();

			System.out.println("executed measurement " + (i + 1) + (warmup ? " (warmup)" : "") + ": " + measuredTime + "ms");
			if (!warmup) {
				stats.addValue(measuredTime);
			}
		}

		System.out.println("\nFinished measurements.\n\n");

		return stats;
	}

	public static void printStats(String title, DescriptiveStatistics stats) {
		System.out.println("Results for " + title);
		System.out.println("\t        N = " + stats.getN());
		System.out.println("\t  min(ms) = " + stats.getMin());
		System.out.printf("\t  avg(ms) = %.3f\n", stats.getMean());
		System.out.println("\t  max(ms) = " + stats.getMax());
		System.out.printf("\tstdev(ms) = %.3f\n", stats.getStandardDeviation());
		System.out.printf("\t stdev(%%) = %.2f\n", stats.getStandardDeviation() / stats.getMean() * 100);
	}

	public static Path createRepeatedInputFile(Path inputFile, int numberOfInputRepeats) throws IOException {
		Path outputFile = Files.createTempFile("io-speedtest", ".tmp");

		byte[] inputBytes = Files.readAllBytes(inputFile);

		OutputStream out = Files.newOutputStream(outputFile);
		for (int i = 0; i < numberOfInputRepeats; i++) {
			out.write(inputBytes);
		}
		out.close();

		return outputFile;
	}

}
