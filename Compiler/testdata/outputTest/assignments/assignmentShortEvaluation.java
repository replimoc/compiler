class Test {
	public static void main(String[] args) {
		Test t = new Test();
		boolean x;

		x = true || t.method(666);
		System.out.println(8);
		t.printBool(x);

		x = false && t.method(777);
		System.out.println(9);
		t.printBool(x);
	}

	public void printBool(boolean b) {
		if (b) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
	}

	public boolean method(int i) {
		System.out.println(i);
		return false;
	}
}