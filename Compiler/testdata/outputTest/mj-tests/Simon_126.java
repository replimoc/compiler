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
		if (n==0 || k==0 || k==n) return 1;
		return nCr(n-1,k)+nCr(n-1,k-1);
	}
}