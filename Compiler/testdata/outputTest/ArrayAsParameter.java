class Test {
	public void m(int[] b) {
		System.out.println(b[0]);
	}

	public static void main(String[] args) {
		Test t = new Test();
		int[] a = new int[5];
		a[0] = 42;
		t.m(a);
	}
}
