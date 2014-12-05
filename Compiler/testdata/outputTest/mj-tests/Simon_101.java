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
		int result = 1;
		while (n > 0) {
			result = result * n;
			n = n - 1;
		}
		return result;
	}
}