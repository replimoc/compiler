class a {
	public int[] x;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		x = new int[4];
		x[3] = 2;
		System.out.println(b(x[3]*(x[3]=3)));
	}
	
	public int b(int y) {
		return y+x[3];
	}
}