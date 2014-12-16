class Test {
	
	public static void main(String[] args) {
		Test t = new Test();
		t.method(4, 5, 6);
	}
	
	public void method(int x, int y, int z) {
		int a = x*y*z;
		a = x*y*z;
		a = x*y*z;
		int b = x*y*z;
		b = x*y*z;
		b = x*y*z;
		int c = a*b;
		c = a*b;
		c = a*b;
		System.out.println(c);
	}
}