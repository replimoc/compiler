class a {
	public static void main(String[] args) {
		int[][] t = new int[4][];
		t[2] = new int[6];
		t[1] = t[2];
		t[2][5] = 3;
		System.out.println(t[1][5]);
		System.out.println(t[2][5]);
	}
}
