class Fortys {
	public int a;

	public static void main(String[] args) {
		Fortys ft = new Fortys();
		ft.a = 42;
		int res = ft.foo(ft);
		System.out.println(res);
	}

	public int foo(Fortys ft) {
		return ft.a + 1;
	}

}
