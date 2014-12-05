class a {
	public int y;
	public int x;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		x = 1;
		y = 2;
		w(4);
		System.out.println(y);
	}
	
	public void w(int y) {
		System.out.println(y);
		System.out.println(x);
		{
			int x = 7;
			System.out.println(x);
		}
		System.out.println(x);
	}
}