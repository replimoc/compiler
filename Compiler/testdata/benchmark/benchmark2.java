class Test {

	public static void main(String[] args) {
		Test t = new Test();
		int[] a = new int[800000];


		int i = 0;
		while (i < 5000) {
			int fib = t.FibonacciIterative(i + 1);
			int max = t.getMaxInt();
			int j = 0;
			while (j < 800000) {
				a[j] = max / fib * j;
				j = j + 1;
			}
			i = i + 1;
		}

		int k = 0;
		int result = 0;
		while (k < 800000) {
			result = result + a[k];
			k = k + 1;
		}
		System.out.println(result);

		int j = 0;
		int[] b = new int[800000];
		while (j < 800000) {
			b[j] = a[j] - j;
			j = j + 1;
		}

		int l = 0;
		int result2 = 0;
		while (l < 800000) {
			result2 = result2 + b[l];
			l = l + 1;
		}

		int x = 0;
		int result3 = 1;
		while (x < 800000) {
			result3 = result3 + result3 * x;
			x = x + 1;
		}

		int y = 0;
		int result4 = 1;
		while (y < 800000) {
			result4 = result4 + result4 * (800000 - y);
			y = y + 1;
		}
		System.out.println(result2);
		System.out.println(result4 - result3);
	}

	public int getMaxInt() {
		int i = 0;
		while (i >= 0) {
			i = i + 1;
		}
		return i - 1;
	}

	public int FibonacciIterative(int n) {
		if (n == 0) {
			return 0;
		}
		if (n == 1) {
			return 1;
		}

		int prevPrev = 0;
		int prev = 1;
		int result = 0;

		int i = 2;
		while (i <= n) {
			result = prev + prevPrev;
			prevPrev = prev;
			prev = result;
			i = i + 1;
		}
		return result;
	}

}
