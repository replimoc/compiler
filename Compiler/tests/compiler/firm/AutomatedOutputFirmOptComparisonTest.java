package compiler.firm;

public class AutomatedOutputFirmOptComparisonTest extends AbstractAutomatedOutputComparisonTest {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--compile-firm" };
	}
}
