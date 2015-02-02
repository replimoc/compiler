class Test {
	public static void main(String[] args) {
		int i = 0;
		
		while(i < 40000) {
			int j = 0;
			while(j < 400000) {
				j = j + 1;
			}
			i = i + 1;
		}
		
		System.out.println(42);
	}
}