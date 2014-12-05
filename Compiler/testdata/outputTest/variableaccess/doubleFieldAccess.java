class Test {
	public int x;
	public Test t;
	
	public static void main(String[] args) {
		Test t = new Test();
		t.t = new Test();
		
		t.t.x = 3;
		
		System.out.println(68);
		System.out.println(t.t.x);
		System.out.println(69);
	}
	
}