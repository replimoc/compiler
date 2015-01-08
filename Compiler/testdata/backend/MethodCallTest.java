class Test {
	public static void main(String[] args) {
		Test t = new Test();
		int b;
		b = t.a(2336);
		System.out.println(b);
	}

	public int a(int a) {
		int c = 10;
		Test d = b();
		System.out.println(a);
		d.d();
		return c;
	}

	public Test b() {
		System.out.println(1337);
		return new Test();
	}

	public void d() {
		System.out.println(42);
	}
}
