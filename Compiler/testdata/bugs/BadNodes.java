class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.method(42);
	}
	
	public void method(int x) {
		int y = x + 1;
		
		while(true) {
			x = y + 1;
			System.out.println(x);
			return;
		}
	}
}