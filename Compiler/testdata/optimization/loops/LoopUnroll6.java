class Test {
	public static void main(String[] args) {
		int i = 29;	
		while(i >= 0) {	
			i = i + 3;
		}
		i = i - 1;
		System.out.println(i);
		
		int j = -17;	
		while(j <= 0) {
			j = j - 4;
		}
		j = j + 1;
		System.out.println(j);
		
		int k = 4;
		while(k <= 9) {
			System.out.println(k);
			k = k + 1;
		}
		
		int a = 6;
		while(a > 4) {
			a = a - 1;
			System.out.println(a);
		}
	}
}