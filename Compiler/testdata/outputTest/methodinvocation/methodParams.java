class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.methodWithParams(1, 2, 3, true, false, t);
	}
	
	public void methodWithParams(int x, int y, int z, boolean a, boolean b, Test t) {
		System.out.println(x);
		System.out.println(y);
		System.out.println(z);
		
		if(a) {
			System.out.println(x+y);
		}
		
		if(b) {
			System.out.println(y+z);
		}
		
		t.emptyMethod();
	}
	
	public void method() {
		System.out.println(10);
	}
}