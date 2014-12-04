class a {
	public int x;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		x = 2;
		System.out.println(b(x*(x=3)));
	}
	
	public int b(int y) {
		return y+x;
	}
}