class Test {
	public int x;
	public boolean y;
	public Test t;
	
	public static void main(String[] args) {
		Test t = new Test();
		
		t.x = 10;
		System.out.println(t.x);
		t.x = t.x + 2;
		System.out.println(t.x);
		
		t.y = true;
		
		if(t.y) {
			System.out.println(14);
		} else {
			System.out.println(16);
		}
		
		t.y = false;
		
		if(t.y) {
			System.out.println(14);
		} else {
			System.out.println(16);
		}
		
		t.t = new Test();
		System.out.println(67);
		t.t.x = t.x + 6;
		System.out.println(68);
		System.out.println(t.t.x);
		System.out.println(69);
	}
	
}