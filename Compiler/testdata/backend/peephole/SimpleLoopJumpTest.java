class AddTest {
	public static void main(String[] args) {
		int i = -100000000;

		while (i < 0) {
			boolean n = true;

			while (n) { // this loop only runs once
				n = i > 0;
				
				i = i - 1;
			}
		}

		System.out.println(i);
	}
}