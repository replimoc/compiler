class Test {
	public static void main(String[] args) {
		int test = 2147483649; /* semantic error, to large */
		int test2 = 2147483640; /* no semantic error */
	}
}
