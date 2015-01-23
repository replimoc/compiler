package compiler.firm;

public class AutomatedOutputBackendNoOptRegComparisonIT extends AbstractAutomatedOutputComparisonIT {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--no-opt" };
	}

	@Override
	protected boolean forkProcess() {
		return false;
	}
}
