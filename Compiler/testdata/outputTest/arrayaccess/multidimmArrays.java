class Test {

	public static void main(String[] args) {
		int[] x1 = new int[10];
		int[] x2 = new int[10];
		int[][] y = new int[2][];

		x1[0] = 5;
		x2[1] = 10;
		y[0] = x1;
		y[1] = x2;

		System.out.println(y[0][0]);
		System.out.println(y[1][1]);
	}
}