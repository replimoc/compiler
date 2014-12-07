class a {
	public int[] x;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		x = new int[3];
		x[2] = 2;
		w();
	}
	
	public void w() {
		System.out.println(x[2]);
	}
}
