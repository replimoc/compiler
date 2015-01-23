package compiler.firm;

public class AutomatedOutputBackendOptRegComparisonIT extends AbstractAutomatedOutputComparisonIT {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] {};
	}

	@Override
	protected boolean forkProcess() {
		return false;
	}
}
