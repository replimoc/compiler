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

	@Test
	public void testVisitAdditionExpression() throws Exception {
		FirmTestUtils.assertExportEquals("firmdata/testAdditionExpression", "firmdata/testAdditionExpression.java", false);
	}

	@Test
	public void testVisitDivisionExpression() throws Exception {
		FirmTestUtils.assertExportEquals("firmdata/testDivisionExpression", "firmdata/testDivisionExpression.java", false);
	}

	@Test
	public void testVisitModuloExpression() throws Exception {
		FirmTestUtils.assertExportEquals("firmdata/testModuloExpression", "firmdata/testModuloExpression.java", false);
	}

	@Test
	public void testVisitMuliplicationExpression() throws Exception {
		FirmTestUtils.assertExportEquals("firmdata/testMultiplicationExpression", "firmdata/testMultiplicationExpression.java", false);
	}

	@Test
	public void testVisitSubtractionExpression() throws Exception {
		FirmTestUtils.assertExportEquals("firmdata/testSubtractionExpression", "firmdata/testSubtractionExpression.java", false);
	}

	@Test
	public void testVisitEqualityExpression() throws Exception {
		FirmTestUtils.assertExportEquals("firmdata/testEqualityExpression", "firmdata/testEqualityExpression.java", false);
	}
}