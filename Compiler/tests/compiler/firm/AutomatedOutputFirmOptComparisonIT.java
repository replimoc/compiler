package compiler.firm;

public class AutomatedOutputFirmOptComparisonIT extends AbstractAutomatedOutputComparisonIT {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--compile-firm" };
	}

	@Override
	protected boolean forkProcess() {
		return true;
	}
}
