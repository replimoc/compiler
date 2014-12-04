class a {
	public int x;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		x = 2;
		e(this);
		e(new a());
	}
	
	public void e(a p) {
		System.out.println(p.x);
	}
}
