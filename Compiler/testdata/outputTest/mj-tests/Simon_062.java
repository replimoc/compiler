class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int x = 2;
		System.out.println(b(x,x=3));
		x = 2;
		System.out.println(b(x=3,x));
		x = 2;
		System.out.println(b(x=4,x=3));
	}
	
	public int b(int x, int y) {
		return x+y;
	}
}