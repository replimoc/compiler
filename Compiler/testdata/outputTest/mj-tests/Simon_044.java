class a {
	public int n;
	public static void main(String[] args) {
		a a1 = new a();
		b b1 = new b();
		b1.n();
		a1.n();
	}
	
	public void n() {
		System.out.println(-23);
	}
}

class b {
	public void n() {
		System.out.println(1);
	}
}
