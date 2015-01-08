class Test {
	public static void main(String[] args) {
		int[] y = new int[32];
		y[10] = 100;
		y[22] = 50;
		System.out.println(y[10]);
		System.out.println(y[22]);
		Test t = new Test();
		t.a(y);
		System.out.println(y[23]);
	}

	public void a(int[] b) {
		System.out.println(b[22]);
		b[23] = 55;
	}
}
