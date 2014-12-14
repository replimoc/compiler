class Test {
	public static void main(String[] args) {
		Test t = new Test();
		System.out.println(t.doNotOptimizeMe(4, 2));
	}
	
	public int doNotOptimizeMe(int z, int a) {
		int x = 0;
		int y = 1;
		
		while(y == 1) {
			/* This phi must not be optimized to const 0! */
			x = (x * z) + a;
			y = y + 1;
		}
		
		return x;
	}
}