class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int i = 0;
		while (i < 7) {
			System.out.println(fact(i));
			i = i + 1;
		}
	}
	
	public int fact(int n) {
		if (n > 0) {
			return n*fact(n-1);
		} else {
			return 1;
		}
	}
}