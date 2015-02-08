package compiler.firm;

public class AutomatedOutputBackendOptNoRegComparisonIT extends AbstractAutomatedOutputComparisonIT {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--no-registers" };
	}

	@Override
	protected boolean forkProcess() {
		return true;
	}
}
