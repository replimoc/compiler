class A {
	public boolean x;
	
	public static void main(String[] args) {
		A a = new A();
		
		a.x = false;
		
		while(a.x) {
			while(!a.x) {
				while(!a.x) {
					System.out.println(0);
				}
			}
		}
		
		while(a.x) {
			if(a.x && !a.x) {
				System.out.println(1);
			} else {
				System.out.println(2);
			}
		}
		
		while(a.x) {
			a.x = true && false;
		}
		
		if(a.x) {
			while(a.x) {
				System.out.println(4);
			}
		} else {
			while(a.x) {
				System.out.println(5);
			}
		}
	}
}