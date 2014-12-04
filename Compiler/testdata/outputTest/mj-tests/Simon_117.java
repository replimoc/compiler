class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int[] a = new int[5];
		a[0] = 32;
		int i = 1;
		while (i<5) {
			System.out.println(a[i-1]);
			a[i] = next(a[i-1]);
			i=i+1;
		}
	}
	
	public int next(int old) {
		return (25173*old+13849)%65536;
	}
}