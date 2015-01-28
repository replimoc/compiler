class Div {

	public static int div(int x, int y) {
		return x / y;
	}

	public static void main(String[] args) {
		int a1 = 4;

		int i = 999;
		while (i < 1000)
		{ /* a1 */
			int exp11 = div(i, a1);
			int res11 = i / a1;

			if (exp11 != res11) {
				System.out.println(exp11);
				System.out.println(res11);
			}
			
			i = i + 1;
		}

		System.out.println(42);
	}

}