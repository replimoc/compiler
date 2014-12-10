class Test {
	public static void main(String[] args) {
		Test t = new Test();
		System.out.println(t.optimizeMe());
	}
	
	public int optimizeMe() {
		int x = 1;
		int y = 1;
		
		while(y == 1) {
			x = 2-x;
			y = y + 1;
		}
		
		return x;
	}
}