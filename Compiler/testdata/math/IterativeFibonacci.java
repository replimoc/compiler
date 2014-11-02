class IterativeFibonacci {

	public static void main(String args[]) {
		IterativeFibonacci fibunacci = new IterativeFibonacci();
		System.out.println(fibunacci.iterativeFib(44));
	}

	int iterativeFib(int n) {
		int[] fibs = new int[n + 1];
		fibs[0] = 0;
		fibs[1] = 1;

		for (int i = 2; i < n + 1; i++) {
			fibs[i] = fibs[i - 1] + fibs[i - 2];
		}

		return fibs[n];
	}
}