class Test {
	public static void main(String[] args) {
		int x;
		Test test = new Test();
		
		if(true || test.method()) {
			x = 42;
		} else {
			x = 17;
		}
		
		System.out.println(x);
	}
	
	public boolean method() {
		System.out.println(24);
		return false;
	}
}
