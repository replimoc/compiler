class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int i = 0;
		while (11>=i) {
			System.out.println(fib(i));
			i=i+1;
		}
	}
	
	public int fib(int n) {
		if (n < 2) return n;
		return fib(n-1)+fib(n-2);
	}
}