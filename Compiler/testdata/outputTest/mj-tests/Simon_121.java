class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int i = 15;
		System.out.println(i);
		while (i != 1) {
			System.out.println(i=f(i));
		}
	}
	
	public int f(int n) {
		if (n%2==0) return n/2;
		else return 3*n+1;
	}		
}