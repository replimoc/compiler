class Test {
	
	public static void main(String[] args) {
		int[] x = new int[10];
		int[][] y = new int[10][];
		
		x[0] = 5;
		x[1] = 10;
		y[0] = x;
		y[1][0] = 5;
		System.out.println(x[0]);
		System.out.println(x[1]);
		System.out.println(y[1][0]);
	}
}