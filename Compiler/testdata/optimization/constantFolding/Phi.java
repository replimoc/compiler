class Test {
	public static void main(String[] args) {
		int x = 1;
		
		if(true) {
			if(true) {
				x = x - 1;
			} else {
				x = 2 - x;
			}
			if(false) {
				x = x - 1;
			} else {
				x = 2 - x;
			}
		}
		System.out.println(x);
	}
}