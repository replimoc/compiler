class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.method(5, 6);
	}
	
	public void method(int x, int y) {
		int z = 0;
		
		while(z < 3) {
			System.out.println(x + y);
			z = z + 1;
		}
	}
}