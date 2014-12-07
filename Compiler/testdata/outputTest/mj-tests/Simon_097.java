class a {
	public static void main(String[] args) {
		boolean b = true;
		int x = 1;
		while (b) {
			System.out.println(x);
			if (x > 100) {
				b = false;
			}
			x = x*2;
		}
	}
}