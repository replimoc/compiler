class a {
	public static void main(String[] args) {
		while (false && new a().q()) {
			System.out.println(3);
		}
		boolean b = true;
		while (b || new a().q()) {
			System.out.println(5);
			b = false;
		}
	}
	
	public boolean q() {
		System.out.println(-2);
		return false;
	}
}