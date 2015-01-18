package compiler.firm;

public class AutomatedOutputBackendNoOptNoRegComparisonTest extends AbstractAutomatedOutputComparisonTest {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--no-opt", "--no-registers" };
	}

	@Override
	protected boolean forkProcess() {
		return false;
	}
}
