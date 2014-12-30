class a {
	public static void main(String[] args) {
		int i = 0;
		int j = -2;
		int z = 1;
		int x = 5;
		int y = 45;
		while (i < 5) {
			int t = i;
			i = j;
			j = t;
			t = z;
			z = x;
			x = y;
			y = i;
			System.out.println(i);
			System.out.println(j);
			System.out.println(z);
			System.out.println(x);
			System.out.println(y);
			i = i + 1;
		}
	}
}