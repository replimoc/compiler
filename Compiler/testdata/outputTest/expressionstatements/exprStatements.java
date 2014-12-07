class Test {
	
	public static void main(String[] args) {
		int x = 42;
		int y = -0;
		int z = -42;
		
		System.out.println(x + 0);
		System.out.println(y - 0);
		System.out.println(x + z);
		
		System.out.println(y / z);
		
		System.out.println(x % 2);
	}
}