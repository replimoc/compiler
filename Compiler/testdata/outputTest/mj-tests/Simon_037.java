class a {
	public static void main(String[] args) {
		int[][] t = new int[4][];
		t[2] = new int[6];
		t[1] = new int[2];
		t[2][5] = 3;
		t[1][1] = 1;
		System.out.println(t[1][1]);
		System.out.println(t[2][5]);
	}
}
