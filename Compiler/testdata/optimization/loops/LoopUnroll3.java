class Test {
	public static void main(String[] args) {
		int[] a = new int[40000];
		int i = 0;
		while(i < 4) {
			a[i] = i / 2;
			i = i + 1;
		}
		
		int j = 0;
		while(j < 4) {
			System.out.println(a[j]);
			j = j + 1;
		}
	}
}