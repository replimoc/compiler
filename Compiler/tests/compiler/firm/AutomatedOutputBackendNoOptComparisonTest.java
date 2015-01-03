package compiler.firm;

public class AutomatedOutputBackendNoOptComparisonTest extends AbstractAutomatedOutputComparisonTest {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--no-opt" };
	}
}
