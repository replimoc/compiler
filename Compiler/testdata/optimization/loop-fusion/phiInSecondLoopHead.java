class a {
	public static void main(String[] args) {
		int i = 0;
		int j = 0;
		int k = 0;
		int[] a = new int[100];
		int[] b = new int[100];
       		int sum1 = 0;
		int sum2 = 0;
		int t = 0;
 
		while (i < 100) {
			a[i] = i;
			b[i] = 100 - i;
			i = i + 1;
		}
		while (j < 100) {
			sum1 = sum1 + a[j];
			j = j + 1;
		}
		while (k < 100) {
			sum2 = sum2 + b[k];
			t = j;
			k = k + 1;
		}
		System.out.println(sum1);
		System.out.println(sum2);
		System.out.println(t);
	}
}
