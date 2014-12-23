class a {
	public static void main(String[] args) {
		int i = 0;
		int j = -2;
		while (i < 5) {
			int t = i;
			i = j;
			j = t;
			System.out.println(i);
			System.out.println(j);
			i = i + 1;
		}
	}
}