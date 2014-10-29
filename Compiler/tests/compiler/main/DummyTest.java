package compiler.main;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DummyTest {

	@Test
	public void test() {
		assertTrue(true);
	}

	@Test
	public void testDummyTestable() {
		assertTrue(new DummyTestable(true).isValue());
		assertFalse(new DummyTestable(false).isValue());

        int a;

        a = 4142;

        a = a + 5;
	}
}
