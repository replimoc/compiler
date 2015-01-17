class Test {
	public static void main(String[] args) {
		Test t = new Test();
		int x = 5;
		
		if(t == t) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
		if(x == x) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
		if(x <= x) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
		if(x >= x) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
		t.method(x);	
	}
	
	public void method(int x) {
		if(x == x) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
		if(x <= x) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
		if(x >= x) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
	}
}