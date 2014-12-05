class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		System.out.println(b(b(b(u(0),u(1)),u(2)),
						   b(b(u(3),u(4)),u(5))));
	}
	
	public int b(int x, int y) {
		return x+y;
	}
	
	public int u(int p) {
		System.out.println(p);
		return 1;
	}
}