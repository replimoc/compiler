class IterativeFibonacci {

	public static void main(String[] args) {
		IterativeFibonacci fibunacci = new IterativeFibonacci();
		System.out.println(fibunacci.iterativeFib(44));
	}

	public int iterativeFib(int n) {
		int[] fibs = new int[n + 1]; // create array for temporary values. 
		fibs[0] = 0;	// init first value with 0
		fibs[1] = 1;	// init second value with 1

		int i = 2;
		while ( i < n + 1) {	// iterate untill the n-th fibonacci is calculated
			fibs[i] = fibs[i - 1] + fibs[i - 2];
			i = i + 1;
		}

		return fibs[n];
	}
}