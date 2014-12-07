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
		int[] i1 = new int[2];
		i1[0] = 1;
		i1[1] = 1;
		int n1 = pow(i1, n)[1];
		i1[1] = -1;
		n1 = n1 - pow(i1, n)[1];
		return n1/2;
	}
	
	public int[] pow(int[] b, int e) {
		int[] r = new int[2];
		r[0]=2;
		if (e==0) return r;
		if (e % 2 == 0) return pow(mult(b,b), e/2);
		return mult(b,pow(mult(b,b), e/2));
	}
	
	public int[] mult(int[] a, int[] b) {
		int[] c = new int[2];
		c[0]=(a[0]*b[0]+5*a[1]*b[1])/2;
		c[1]=(a[1]*b[0]+a[0]*b[1])/2;
		return c;
	}
}