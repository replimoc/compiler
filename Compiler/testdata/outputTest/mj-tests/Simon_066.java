class a {
	public static void main(String[] args) {
		int[] x = new int[4];
		x[x[2]=2] = 4;
		System.out.println(x[2]);
		x[2]=9;
		x[x[2]=3] = x[2];
		System.out.println(x[2]);
		System.out.println(x[3]);
		x[3]=6;
		x[x[2]=3] = x[x[3]=2] = x[3];
		System.out.println(x[2]);
		System.out.println(x[3]);
		x[3] = 2;
		x[x[2]=2] = x[x[3]] = x[3] = 5;
		System.out.println(x[2]);
		System.out.println(x[3]);
	}
}