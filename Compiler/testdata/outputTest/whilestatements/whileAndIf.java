class Test {
	public static void main(String[] args) {
		
		if(true) {
			while(true) {
				System.out.println(1);
				if(true) {
					System.out.println(2);
				} else {
					System.out.println(3);
				}		
				System.out.println(3);
				return;
				System.out.println(-6);
			}
		} else {
			while(false) {
				System.out.println(-6);
			}
		}
		System.out.println(-6);
		return;
	}
}