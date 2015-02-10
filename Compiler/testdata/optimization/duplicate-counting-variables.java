class a {
	public static void main(String[] args) {
		int i = 0;
		int j = 0;
		int[] a = new int[100];
        
		while (i < 100) {
			a[i] = i;
			i = i + 1;
			j = j + 1;
		}
		System.out.println(a[10]);
	}
}
