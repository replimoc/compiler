class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		System.out.println(nCr(0,0));
		System.out.println(nCr(1,0));
		System.out.println(nCr(1,1));
		System.out.println(nCr(2,0));
		System.out.println(nCr(2,1));
		System.out.println(nCr(2,2));
		System.out.println(nCr(3,0));
		System.out.println(nCr(3,1));
		System.out.println(nCr(3,2));
		System.out.println(nCr(3,3));
		System.out.println(nCr(4,2));
	}
	
	public int nCr(int n, int k) {
		return fact(n)/fact(k)/fact(n-k);
	}
	
	public int fact(int n) {
		int result = 1;
		while (n > 0) {
			result = result * n;
			n = n - 1;
		}
		return result;
	}
}