class Test {

	public native void print_int(int i);
	
    public static void main(String[] args) {
		Test t = new Test();
		
		t.print_int(-1);
		t.print_int(2);
    }

}