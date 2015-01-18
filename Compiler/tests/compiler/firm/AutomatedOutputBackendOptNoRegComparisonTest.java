package compiler.firm;

public class AutomatedOutputBackendOptNoRegComparisonTest extends AbstractAutomatedOutputComparisonTest {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--no-registers" };
	}

	@Override
	protected boolean forkProcess() {
		return false;
	}
}
