class Test {
	public void test(int test) { }
	public void test2(boolean test) { }
	public void test3() {
		test(null);
		test2(null);
	}
	public static void main(String[] args) {
		System.out.println(null);

		Test test = new Test();
		test.test(null);
		test.test2(null);
	}
}
