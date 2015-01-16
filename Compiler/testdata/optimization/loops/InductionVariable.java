class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.method();
	}
	
	public void method() {
		int z = 0;
		int x = 3;
		int y = 0;

		while(z < 5) {
			y = x * z;
			System.out.println(y);
			z = z + 1;
		}
	}
}