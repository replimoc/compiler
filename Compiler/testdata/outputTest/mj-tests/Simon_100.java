class a {
	public static void main(String[] args) {
		boolean b = true;
		int x = 5;
		if (b) {
			while (x < 10) {
				x=x+1;
				if (x == 7) {
					System.out.println(-7);
				} else {
					while (b) {
						System.out.println(-1);
						b = false;
					}
				}
				System.out.println(x);
			}
		}
	}
}