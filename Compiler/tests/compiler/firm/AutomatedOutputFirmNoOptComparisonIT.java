package compiler.firm;

public class AutomatedOutputFirmNoOptComparisonIT extends AbstractAutomatedOutputComparisonIT {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--compile-firm", "--no-opt" };
	}

	@Override
	protected boolean forkProcess() {
		return true;
	}
}
