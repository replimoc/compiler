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
		int n0=0;
		int n1=1;
		while (n>0) {
			int n2 = n1+n0;
			n0=n1;
			n1=n2;
			n=n-1;
		}
		return n0;
	}
}