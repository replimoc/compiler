class Test {
	public static void main(String[] args) {
		int x;
		Test test = null:
		
		if(true || test.method()) {
			x = 42;
		} else {
			x = 17;
		}
		
		System.out.println(x);
	}
	
	public void method() {}
}