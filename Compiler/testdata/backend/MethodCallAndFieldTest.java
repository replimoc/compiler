class Test {
	public int d42;

	public static void main(String[] args) {
		Test t = new Test();
		int b;
		int c;
		t.d42 = 1907;
		b = t.a(11, new Test(), 12, 13);
		System.out.println(b);
	}

	public int a(int a, Test t21, int z, int y) {
		int c = 10;
		Test d = b(1312);
		System.out.println(d42);
		System.out.println(a);
		System.out.println(z);
		System.out.println(y);
		d.d42 = 43;
		t21.d42 = 1338;
		d.d();
		return c;
	}

	public Test b(int b) {
		System.out.println(b);
		System.out.println(1337);
		return new Test();
	}

	public void d() {
		System.out.println(42);
		System.out.println(d42);
	}
}
