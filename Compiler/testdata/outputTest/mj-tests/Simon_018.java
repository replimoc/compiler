class a {
	public int x;
	public static void main(String[] args) {
		a a1 = new a();
		a a2 = new a();
		a1.x = 2;
		a2.x = 3;
		System.out.println(a2.x);
		System.out.println(a1.x);
	}
}
