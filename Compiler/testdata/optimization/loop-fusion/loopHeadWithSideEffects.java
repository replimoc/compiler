class a {
	public static void main(String[] args) {
		int i = 0;
		int j = 0;
		int[] a = new int[120];
		int[] b = new int[120];
		int res = 0;

		int k = 0;
		while (k < 101) {
			b[k] = k + 2;
			k = k + 1;
		}

		while ((a[i] = i) < 100) {
			i = i + 1;
		}
		while ((a[j+10] = b[j]) < 100) {
			res = a[j+1];
			j = j + 1;
		}
		System.out.println(a[10]);
		System.out.println(b[20]);
		System.out.println(res);
	}
}
