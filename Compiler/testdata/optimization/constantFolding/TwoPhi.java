class Test {
	public static void main(String[] args) {
		Test t = new Test();
		System.out.println(t.doNotOptimizeMe(4, 2));
	}
	
	public int doNotOptimizeMe(int a, int b) {
		int x = 0;
		int y = 1;
		int z = 5;
		
		while(y == 1) {
			/* x and y are const on their first visit */
			/* the add must not be optimized */
			z = x + y;
			y = y + 1;
			if(a > b) {
				x = x + 1;
			} else {
				x = x + 0;
			}
		}
		
		return z;
	}
}