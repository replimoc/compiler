class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.method();
	}
	
	public void method() {
		int z = 0;
		int a = 0;
		int[] x = new int[2];
		
		while(z < 400000000) {
			while(a < 400000000) {
				x[1] = 17;
				a = a + 1;
			}
			z = z + 1;
		}
		System.out.println(x[1]);
	}
}