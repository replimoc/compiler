package compiler.firm;

public class AutomatedOutputBackendOptRegComparisonTest extends AbstractAutomatedOutputComparisonTest {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] {};
	}

	@Override
	protected boolean forkProcess() {
		return false;
	}
}
