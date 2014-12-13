class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.optimizeMe(5);
	}
	
	public void optimizeMe(int x) {
		int y = x / 1;
		int z = y % 1;
		
		System.out.println(y);
		System.out.println(z);
	}
}