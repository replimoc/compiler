class a {
	public static void main(String[] args) {
		int[] w = new int[5];
		w[2] = 3;
		new a().q(w);
		System.out.println(w[1]);
	}
	
	public void q(int[] w) {
		int r = w[2];
		System.out.println(r);
		w[1] = -10;
	}
}
