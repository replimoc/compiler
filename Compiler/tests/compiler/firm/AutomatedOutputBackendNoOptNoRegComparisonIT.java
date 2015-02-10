package compiler.firm;

public class AutomatedOutputBackendNoOptNoRegComparisonIT extends AbstractAutomatedOutputComparisonIT {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--no-opt", "--no-registers" };
	}

	@Override
	protected boolean forkProcess() {
		return true;
	}
}
