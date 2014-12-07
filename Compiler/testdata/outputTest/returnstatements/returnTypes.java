class Test {
	
	public static void main(String[] args) {
		Test t = new Test();

		if(t.conditionTrue()) {
			System.out.println(t.method(3));
		} else {
			System.out.println(t.method(6));
		}

		if(t.referenceNull() == t.reference()) {
			System.out.println(t.method(3));
		} else {
			System.out.println(t.method(6));
		}
	}
	
	public int method(int x) {
		return x;
	}
	
	public boolean conditionTrue() {
		return true;
	}
	
	public Test referenceNull() {
		return null;
	}
	
	public Test reference() {
		return this;
	}
}