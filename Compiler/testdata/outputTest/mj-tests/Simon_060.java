class a {
	public static void main(String[] args) {
		int[][][] x = new int[3][][];
		x[0] = new int[4][];
		int[] y = new int[3];
		x[0][1] = y;
		y[2] = 7;
		x[1] = x[0];
		x[2] = new int[5][];
		x[2][3] = new int[2];
		x[2][3][0] = 4;
		x[2][1] = y;
		x[2][1][1] = -4;
		x[0][2] = x[2][3];
		x[0][2][1] = 1;
		System.out.println(x[0][1][2]);
		System.out.println(x[0][1][1]);
		System.out.println(x[0][2][0]);
		System.out.println(x[0][2][1]);
		System.out.println(x[1][1][2]);
		System.out.println(x[1][1][1]);
		System.out.println(x[1][2][0]);
		System.out.println(x[1][2][1]);
		System.out.println(x[2][1][1]);
		System.out.println(x[2][1][2]);
		System.out.println(x[2][3][0]);
		System.out.println(x[2][3][1]);
		System.out.println(y[1]);
		System.out.println(y[2]);
	}
}