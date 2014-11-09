package compiler.performance;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class PerformanceUtils {
	public static DescriptiveStatistics executeMeasurements(Measurable measurable, final int numMeasures, final int numWarmup) throws Exception {
		DescriptiveStatistics stats = new DescriptiveStatistics();

		// warm up system to reduce variations, which are enormous in first runs.
		for (int j = 0; j < numWarmup; j++)
		{
			measurable.measure();
		}

		// execute actuall measurement
		for (int j = 0; j < numMeasures; j++)
		{
			long measuredTime = measurable.measure();
			stats.addValue(measuredTime);
		}

		return stats;
	}

	public static void printStats(String title, DescriptiveStatistics stats) {
		System.out.println("results for " + title);
		System.out.println("\t  min(ms) = " + stats.getMin());
		System.out.printf("\t  avg(ms) = %.3f\n", stats.getMean());
		System.out.println("\t  max(ms) = " + stats.getMax());
		System.out.printf("\tstdev(ms) = %.3f\n", stats.getStandardDeviation());
		System.out.printf("\t stdev(%%) = %.2f\n", stats.getStandardDeviation() / stats.getMean() * 100);
	}

}
