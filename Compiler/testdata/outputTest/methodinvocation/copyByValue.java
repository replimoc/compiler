class Test {
	public static void main(String[] args) {
		int x = 10;
		System.out.println(x);
		Test t = new Test();
		t.method(x);
		System.out.println(x);
	}
	
	public void method(int x) {
		x = x + x;
		System.out.println(x);
	}
}