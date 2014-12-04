class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int i = 95;
		while (i<=105) {
			System.out.println(m(i));
			i=i+1;
		}
		System.out.println(m(0));
	}
	
	public int m(int j) {
		if (j > 100) return j-10;
		return m(m(j+11));
	}
}