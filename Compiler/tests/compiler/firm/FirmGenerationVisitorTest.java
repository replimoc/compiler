package compiler.firm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FirmGenerationVisitorTest {
	@Before
	public void setUp() throws Exception {
		FirmUtils.initFirm();
	}

	@After
	public void tearDown() throws Exception {
		FirmUtils.finishFirm();
	}

	private void testFile(String name, boolean overwrite) throws Exception {
		FirmTestUtils.assertExportEquals(name, name + ".java", overwrite);
	}

	@Test
	public void testVisitAdditionExpression() throws Exception {
		testFile("firmdata/expression/testAdditionExpression", false);
	}

	@Test
	public void testVisitDivisionExpression() throws Exception {
		testFile("firmdata/expression/testDivisionExpression", false);
	}

	@Test
	public void testVisitModuloExpression() throws Exception {
		testFile("firmdata/expression/testModuloExpression", false);
	}

	@Test
	public void testVisitMuliplicationExpression() throws Exception {
		testFile("firmdata/expression/testMultiplicationExpression", false);
	}

	@Test
	public void testVisitSubtractionExpression() throws Exception {
		testFile("firmdata/expression/testSubtractionExpression", false);
	}

	@Test
	public void testVisitEqualityExpression() throws Exception {
		testFile("firmdata/comparison/testEqualityExpression", false);
	}
}
