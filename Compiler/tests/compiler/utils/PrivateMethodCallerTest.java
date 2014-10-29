package compiler.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests for {@link PrivateMethodCaller} class. This tests can also be used to see how to use the class.
 * 
 * @author Andreas Eberle
 *
 */
public class PrivateMethodCallerTest {

	private final PrivateMethodCaller caller = new PrivateMethodCaller(PrivateMethodCallerTestDummy.class);
	private final PrivateMethodCallerTestDummy dummy = new PrivateMethodCallerTestDummy();

	@Test(expected = RuntimeException.class)
	public void testCallNonExistingMethod() {
		caller.call("nonExistingStaticMethod", null);
		fail();
	}

	@Test
	public void testPrivateStaticNoArgs() {
		String result = caller.call("privateStaticNoArgs", null);
		assertEquals("Hello World", result);
	}

	@Test
	public void testPrivateStaticWithArgs() {
		String result = caller.call("privateStatic2Args", null, new Class<?>[] { String.class, int.class }, new Object[] { "Bernd", 18 });
		assertEquals("Hi Bernd, you are 18", result);
	}

	@Test
	public void testPrivateNonStaticNoArgs() {
		String result = caller.call("privateNonStaticNoArgs", null);
		assertEquals("Hello World", result);
	}

	@Test
	public void testPrivateNonStaticWithArgs() {
		int int1 = 5, int2 = 13;
		int result = caller.call("privateNonStatic2Args", dummy, new Class<?>[] { int.class, int.class }, new Object[] { int1, int2 });
		assertEquals(int1 + int2, result);
	}

	@Test
	public void testPrivateNonStaticWithArgsOverloaded() {
		String result = caller.call("privateNonStatic2Args", dummy, new Class<?>[] { String.class, float.class }, new Object[] { "Bernd", 9.3443f });
		assertEquals("Bernd 9.3443", result);
	}

	public static class PrivateMethodCallerTestDummy {

		@SuppressWarnings("unused")
		private static String privateStaticNoArgs() {
			return "Hello World";
		}

		@SuppressWarnings("unused")
		private static String privateStatic2Args(String name, int age) {
			return "Hi " + name + ", you are " + age;
		}

		@SuppressWarnings("unused")
		private static String privateNonStaticNoArgs() {
			return "Hello World";
		}

		@SuppressWarnings("unused")
		private int privateNonStatic2Args(int int1, int int2) {
			return int1 + int2;
		}

		@SuppressWarnings("unused")
		private String privateNonStatic2Args(String stringParam, float floatParam) {
			return stringParam + " " + floatParam;
		}
	}
}
