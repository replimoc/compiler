class Test {
	public static void main(String[] args) {
		boolean a = false || true;	
		boolean b = true || a;	
		boolean c = false && b;
		boolean d = b || c;
		
		if(d) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
		
	}
}