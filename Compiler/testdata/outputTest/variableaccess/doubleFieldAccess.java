class Test {
	public int x;
	public Test t;
	
	public static void main(String[] args) {
		Test t = new Test();
		System.out.println(66);
		t.t = new Test();

		System.out.println(67);
		t.t.x = 3;
		
		System.out.println(68);
		System.out.println(t.t.x);
		System.out.println(69);
	}
	
}