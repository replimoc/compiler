class a {
	public int n;
	public static void main(String[] args) {
		a a1 = new a();
		b b1 = new b();
		a1.n = 2;
		b1.n = new int[3];
		b1.n[a1.n] = 1;
		System.out.println(a1.n);
		System.out.println(b1.n[2]);
	}
}

class b {
	public int[] n;
}
