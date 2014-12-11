class Test {
	public static void main(String[] args) {
		Test t = new Test();
		System.out.println(t.optimizeMe(4, 2));
		System.out.println(t.doNotOptimizeMe(4, 2));
		System.out.println(t.doNotOptimizeMe2(4, 2));
	}
	
	public int optimizeMe(int z, int a) {
		int x = 1;
		int y = 1;
		
		while(y < 4) {
			x = x + 1;
			y = y + 1;
			return x + 1;
		}
		
		return 0;
	}
	
	public int doNotOptimizeMe(int z, int a) {
		int x = 1;
		int y = 1;
		
		while(y < 4) {
			/* This x must not be optimized */
			x = x + 1;
			y = y + 1;
		}
		
		return x + 1;
	}
	
	public int doNotOptimizeMe2(int z, int a) {
		int x = 1;
		int y = 1;
		int b = 0;
		
		while(y < 4) {
			/* This x must not be optimized */
			x = x + 1;
			y = y + 1;
			b = x + 1;
		}
		
		return b + 1;
	}
}