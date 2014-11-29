package compiler.firm;

import static org.junit.Assert.fail;

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
		FirmTestUtils.assertExportEquals("firmdata/testAdditionExpression", "firmdata/testAdditionExpression.java");
	}

	@Test
	public void testVisitDivisionExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitModuloExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitMuliplicationExpression() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testVisitSubtractionExpression() {
		fail("Not yet implemented"); // TODO
	}

}
