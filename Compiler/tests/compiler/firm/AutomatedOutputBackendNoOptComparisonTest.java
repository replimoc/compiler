package compiler.firm;

public class AutomatedOutputBackendNoOptComparisonTest extends AutomatedOutputComparisonTest {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--no-opt" };
	}
}
