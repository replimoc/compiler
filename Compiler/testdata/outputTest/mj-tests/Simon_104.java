class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		System.out.println(mult(12,12));
		System.out.println(mult(3,15));
		System.out.println(mult(4,7));
		System.out.println(mult(8,4));
		System.out.println(mult(7,7));
	}
	
	public int mult(int n, int m) {
		int result = 0;
		while (n > 0) {
			if (n % 2 != 0) {
				result = result + m;
			}
			m = m * 2;
			n=n/2;
		}
		return result;
	}
}