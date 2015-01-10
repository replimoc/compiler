class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.method(1);
	}
	
	public void method(int x) {		
		int z = x * 5;
		int a = 5 * x;
		
		System.out.println(z);
		System.out.println(a);
	}
}