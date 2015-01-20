class a {
	public static void main(String[] args) {
		System.out.println(new a().a(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
	}

	public int a(int a, int b, int c, int d, int e, int f, int g, int h, int j, int k, int l, int m) {
		System.out.println(h);
		System.out.println(j);
		System.out.println(k);
		System.out.println(l);
		System.out.println(m);
		return b(a, b, c, d, e, f, g);
	}

	public int b(int a, int b, int c, int d, int e, int f, int g) {
		System.out.println(g);
		return c(a, b, c, d, e, f);
	}

	public int c(int a, int b, int c, int d, int e, int f) {
		System.out.println(f);
		return d(a, b, c, d, e);
	}

	public int d(int a, int b, int c, int d, int e) {
		System.out.println(e);
		return e(a, b, c, d);
	}

	public int e(int a, int b, int c, int d) {
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		return f();
	}

	public int f() {
		return 42;
	}
}
