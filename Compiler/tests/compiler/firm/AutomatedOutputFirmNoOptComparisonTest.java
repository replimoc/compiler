package compiler.firm;

public class AutomatedOutputFirmNoOptComparisonTest extends AbstractAutomatedOutputComparisonTest {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--compile-firm", "--no-opt" };
	}

	@Override
	protected boolean forkProcess() {
		return true;
	}
}
