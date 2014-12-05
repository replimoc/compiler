class Test {
	public static void main(String[] args) {
		
		while(true) {
			System.out.println(1);
			if(true) {
				System.out.println(2);
			} else {
				System.out.println(3);
			}
			
			if(false) {
				System.out.println(2);
			} else {
				System.out.println(3);
			}
			
			if(true) {
				System.out.println(4);
			}
			
			if(true || false) {
				System.out.println(5);
				return;
			} else {
				System.out.println(6);
			}
			
			System.out.println(6);
			return;
			System.out.println(-6);
		}
	}
}