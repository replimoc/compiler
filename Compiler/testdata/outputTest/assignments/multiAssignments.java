class Test {
	public static void main(String[] args) {
		int[] x = new int[5];

		x[1] = 10;
		x[1] = 11;
		x[2] = 12;
		x[3] = 13;
		x[1] = x[x[0] = x[3] = x[4] = 3] = x[2];

		System.out.println(x[0]);
		System.out.println(x[1]);
		System.out.println(x[2]);
		System.out.println(x[3]);
		System.out.println(x[4]);
	}
}