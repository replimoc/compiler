class Test {
	public int x;
	public static void main(String[] args) {
		Test t = new Test();
		t.x = 10;
		Test t2 = new Test();
		t2.x = 15;
		
		if(t == t2) {
			System.out.println(1);
		} else {
			System.out.println(2);
		}
		
		if(t.x == t2.x) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
		Test t3 = t;
		if(t == t3) {
			System.out.println(5);
		} else {
			System.out.println(6);
		}
		System.out.println(t3.x);
	}
}