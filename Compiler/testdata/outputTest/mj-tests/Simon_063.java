class a {
	public int x;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		a y = new a();
		y.x = 2;
		System.out.println(b(y.x,y.x=3));
		y.x = 2;
		System.out.println(b(y.x=3,y.x));
		y.x = 2;
		System.out.println(b(y.x=4,y.x=3));
	}
	
	public int b(int x, int y) {
		return x+y;
	}
}