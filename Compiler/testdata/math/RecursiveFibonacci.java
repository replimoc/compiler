class RecursiveFibonacci {

	public static void main(String[] args) {
		RecursiveFibonacci fibunacci = new RecursiveFibonacci();
		System.out.println(fibunacci.recursiveFib(10));
	}

	public int recursiveFib(int n) {
		if (n <= 2) {
			return 1;
		}

		return recursiveFib(n - 1) + recursiveFib(n - 2);
	}
}