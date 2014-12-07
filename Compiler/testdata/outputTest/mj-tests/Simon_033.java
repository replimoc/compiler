class a {
	public static void main(String[] args) {
		int[] t = new a().q();
		System.out.println(t[2]);
	}
	
	public int[] q() {
		int[] w = new int[5];
		w[2] = 3;
		return w;
	}
}
