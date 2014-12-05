class a {
	public a x;
	public int n;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		a y = s(1).s(2).s(3);
		System.out.println(y.n);
	}
	
	public a s(int w) {
		x = new a();
		System.out.println(w);
		x.n = w;
		return x;
	}
}
