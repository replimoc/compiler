class Test {
	public static void method(int x, int y) {
		int z = 0;
		
		while(z < 3) {
			System.out.println(x + y);
			z = z + 1;
		}
	}
	
	public static void main(String[] args) {
		method(5, 6);
	}
}