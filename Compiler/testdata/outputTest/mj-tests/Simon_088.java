class a {
	public static void main(String[] args) {
		if (false && new a().q()) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
		if (true || new a().q()) {
			System.out.println(5);
		} else {
			System.out.println(6);
		}
	}
	
	public boolean q() {
		System.out.println(-2);
		return true;
	}
}