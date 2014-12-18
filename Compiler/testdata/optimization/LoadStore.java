class Test {
	public int x;
	public int y;
	public boolean a;
	public boolean b;
	public Test test;
	
	public static void main(String[] args) {
		Test t = new Test();
		t.method();
	}
	
	public void method() {
		x = y + 17;
		y = x + 42;
		test = new Test();
		boolean c = a || b || b || a || b;
		x = x * x * y;
		
		if(c) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
		System.out.println(x);
	}
}