class Test {
	public static void main(String[] args) {
		Test t = new Test();
		System.out.println(t.optimizeMe(4, 2));
		System.out.println(t.doNotOptimizeMe(4, 2));
	}
	
	public int optimizeMe(int z, int a) {
		int x = 0;
		int y = 1;
		
		while(y == 1) {
			/* x is always 0 even if a is not known */
			x = x / a;
			y = y + 1;
		}
		
		return x;
	}
	
	public int doNotOptimizeMe(int z, int a) {
		int x = 0;
		int y = 1;
		
		while(y == 1) {
			/* x is 0 on the first visit, but not afterwards */
			/* the div must not be optimized */
			x = x / a;
			y = y + 1;
			if(z > a) {
				x = x + 1;
			} else {
				x = x + 0;
			}
		}
		
		return x;
	}
}