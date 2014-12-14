class Integer {
	public static void main(String[] args) {
		int min = -2147483648;
		int max = 2147483647;

		int a = -max - 1;
		if (min == a) {
			System.out.println(42);
		} else {
			System.out.println(min);
		}
	}
}
