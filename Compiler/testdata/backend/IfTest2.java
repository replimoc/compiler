class If1 {
	public static void main(String[] args) {
		int x = 5;
		int y = 7;
		if (x < 10) {
			System.out.println(1);
		} else {
			if (y < 10) {
				System.out.println(2);
			} else {
				System.out.println(3);
			}
		}
	}
}