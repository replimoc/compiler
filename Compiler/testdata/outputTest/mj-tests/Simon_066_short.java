class a {
	public static void main(String[] args) {
		int[] x = new int[4];
		x[2] = 9;
		x[x[2] = 3] = x[2];
		System.out.println(x[2]);
		System.out.println(x[3]);
	}
}