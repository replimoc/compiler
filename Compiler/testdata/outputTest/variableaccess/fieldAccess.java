class Test {
	public int x;
	public boolean y;
	public Test t;
	
	public static void main(String[] args) {
		x = 10;
		System.out.println(x);
		x = x + 2;
		System.out.println(x);
		
		y = true;
		
		if(y) {
			System.out.println(14);
		} else {
			System.out.println(16);
		}
		
		y = false;
		
		if(y) {
			System.out.println(14);
		} else {
			System.out.println(16);
		}
		
		t = new Test();
		t.x = x + 6;
		System.out.println(t.x);
	}
	
}