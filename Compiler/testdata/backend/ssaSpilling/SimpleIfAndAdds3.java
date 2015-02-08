/*
 * Use with 2 registers available for the register allocation
 */

class a {

	public static void main(String[] args) {
		int x = 1;
		int y = 2;
		int v = 3;
		int z = 4;
		
		if(1 == 1){
			System.out.println(v);
			v = v + 2;
			x = y + 1;
			z = 1;
			System.out.println(z);
		} 
		
		y = z + x;
		System.out.println(x);
		System.out.println(y);
	}
}
