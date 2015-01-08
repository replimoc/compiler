class Test {
	public static void main(String[] args) {
		Test.print(1);

		Test.testStatic1(2);
		Test.testStatic2(3);

		Test tm = new Test();
		tm.print(5);
	}

	public static void testStatic1(int i) {
		Test.print(i);
	}

	public static void testStatic2(int i) {
		Test t = new Test();
		t.print(i);
	}

	public void print(int i) {
		System.out.println(i);
	}
}