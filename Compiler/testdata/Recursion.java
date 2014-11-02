class Fibonacci {

	public static void main(String[] args) {
		Fibonacci fib = new Fibonacci();
		int x = fib.fibonacci(42);
		System.out.println(x);
	}

	public int fibonacci(int number) {
		if (number == 1 || number == 2) {
			return 1;
		}
		return fibonacci(number - 1) + fibonacci(number - 2);
	}
}