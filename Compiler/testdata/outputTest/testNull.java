class Test {
	
	public static void main(String[] args) {
		Test test = null;
		
		if(test == null) {
			System.out.println(7);
		} else {
			System.out.println(9);
		}
		
		if(test != null) {
			System.out.println(13);
		} else {
			System.out.println(15);
		}
		
		if(null == null) {
			System.out.println(19);
		} else {
			System.out.println(21);
		}
		
		if(null != null) {
			System.out.println(25);
		} else {
			System.out.println(27);
		}
	}
}