/*
 * Use with 2 registers available for the register allocation
 */

class a {

	public static void main(String[] args) {
		int x = 1+1;
		int y = 2+1;
		int v = 3+1;
		int z = x + y + v;
		
		if(1 == 1){
			System.out.println(v);
			System.out.println(z);
		} else {
			System.out.println(x);
			System.out.println(y);
		}
	}
}
