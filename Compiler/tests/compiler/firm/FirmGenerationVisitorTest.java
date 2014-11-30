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
	public void testVisitNegateExpression() throws Exception {
		testFile("firmdata/expression/testNegateExpression", false);
	}

	@Test
	public void testVisitEqualityExpression() throws Exception {
		testFile("firmdata/comparison/testEqualityExpression", false);
	}

	@Test
	public void testVisitNonEqualityExpression() throws Exception {
		testFile("firmdata/comparison/testNonEqualityExpression", false);
	}

	@Test
	public void testVisitGreaterThanEqualExpression() throws Exception {
		testFile("firmdata/comparison/testGreaterThanEqualExpression", false);
	}

	@Test
	public void testVisitGreaterThanExpression() throws Exception {
		testFile("firmdata/comparison/testGreaterThanExpression", false);
	}

	@Test
	public void testVisitLessThanEqualExpression() throws Exception {
		testFile("firmdata/comparison/testLessThanEqualExpression", false);
	}

	@Test
	public void testVisitLessThanExpression() throws Exception {
		testFile("firmdata/comparison/testLessThanExpression", false);
	}
}
