package compiler.firm;

public class AutomatedOutputNoOptComparisonTest extends AutomatedOutputComparisonTest {

	@Override
	protected String[] getAdditionalOptions() {
		return new String[] { "--compile-firm", "--no-opt" };
	}
}
