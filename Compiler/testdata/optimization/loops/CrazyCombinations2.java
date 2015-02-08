class Test {
	public static void main(String[] args) {
		int i = -2147483648;
		while (i < 2147483647) {
			i = i + 1;
		}
		System.out.println(i);
		intGreaterLoop(3,    5,   1);
		intGreaterLoop(5,    3,   1);
		intGreaterLoop(3,    3,   1);
		intGreaterLoop(-10,  3,   1);
		intGreaterLoop(3,    5,   7);
		intGreaterLoop(5,    3,   7);
		intGreaterLoop(3,    3,   7);
		intGreaterLoop(-10,  3,   7);

		intGreaterLoop(3,    5,   -1);
		intGreaterLoop(5,    3,   -1);
		intGreaterLoop(3,    3,   -1);
		intGreaterLoop(-10,  3,   -1);
		intGreaterLoop(3,    5,   -7);
		intGreaterLoop(5,    3,   -7);
		intGreaterLoop(3,    3,   -7);
		intGreaterLoop(-10,  3,   -7);

		intGreaterEqualLoop(3,    5,   1);
		intGreaterEqualLoop(5,    3,   1);
		intGreaterEqualLoop(3,    3,   1);
		intGreaterEqualLoop(-10,  3,   1);
		intGreaterEqualLoop(3,    5,   7);
		intGreaterEqualLoop(5,    3,   7);
		intGreaterEqualLoop(3,    3,   7);
		intGreaterEqualLoop(-10,  3,   7);

		intGreaterEqualLoop(3,    5,   -1);
		intGreaterEqualLoop(5,    3,   -1);
		intGreaterEqualLoop(3,    3,   -1);
		intGreaterEqualLoop(-10,  3,   -1);
		intGreaterEqualLoop(3,    5,   -7);
		intGreaterEqualLoop(5,    3,   -7);
		intGreaterEqualLoop(3,    3,   -7);
		intGreaterEqualLoop(-10,  3,   -7);

		intSmallerLoop(3,    5,   1);
		intSmallerLoop(5,    3,   1);
		intSmallerLoop(3,    3,   1);
		intSmallerLoop(-10,  3,   1);
		intSmallerLoop(3,    5,   7);
		intSmallerLoop(5,    3,   7);
		intSmallerLoop(3,    3,   7);
		intSmallerLoop(-10,  3,   7);


		intSmallerLoop(3,    5,   -1);
		intSmallerLoop(5,    3,   -1);
		intSmallerLoop(3,    3,   -1);
		intSmallerLoop(-10,  3,   -1);
		intSmallerLoop(3,    5,   -7);
		intSmallerLoop(5,    3,   -7);
		intSmallerLoop(3,    3,   -7);
		intSmallerLoop(-10,  3,   -7);
		intGreaterLoop(-5,   -3,   7);
		intSmallerLoop(-5,   -3,   7);
		intSmallerLoop(-10,  3,   -1);
		intSmallerLoop(-10,  3,   -1);
		intGreaterLoop(5,    3,   1);
		intSmallerLoop(-10,  3,   -7);

		intSmallerEqualLoop(3,    5,   1);
		intSmallerEqualLoop(5,    3,   1);
		intSmallerEqualLoop(3,    3,   1);
		intSmallerEqualLoop(-10,  3,   1);
		intSmallerEqualLoop(3,    5,   7);
		intSmallerEqualLoop(5,    3,   7);
		intSmallerEqualLoop(3,    3,   7);
		intSmallerEqualLoop(-10,  3,   7);
		intSmallerEqualLoop(3,    5,   -1);
		intSmallerEqualLoop(5,    3,   -1);
		intSmallerEqualLoop(3,    3,   -1);
		intSmallerEqualLoop(-10,  3,   -1);
		intSmallerEqualLoop(3,    5,   -7);
		intSmallerEqualLoop(5,    3,   -7);
		intSmallerEqualLoop(3,    3,   -7);
		intSmallerEqualLoop(-10,  3,   -7);

		intSmallerEqualLoop(3,    3,   -7);
	}

	public static void intGreaterEqualLoop(int start, int border, int step) {
		int i = start;
		while (i >= border) {
			i = i + step;
		}
		System.out.println(i);
	}

	public static void intSmallerEqualLoop(int start, int border, int step) {
		int i = start;
		while (i <= border) {
			i = i + step;
		}
		System.out.println(i);
	}

	public static void intGreaterLoop(int start, int border, int step) {
		int i = start;
		while (i > border) {
			i = i + step;
		}
		System.out.println(i);
	}

	public static void intSmallerLoop(int start, int border, int step) {
		int i = start;
		while (i < border) {
			i = i + step;
		}
		System.out.println(i);
	}
}
