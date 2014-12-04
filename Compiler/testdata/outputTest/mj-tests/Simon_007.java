class a {
	public static void main(String[] args) {
		new a().last();
		new a().first();
		new a().last();
	}
	
	public void last() {
		printUpper(2);
		printLower(15);
		printLower(14);
		printLower(4);
	}
	
	public void first() {
		printUpper(10);
		printLower(1);
		printLower(13);
		printLower(5);
		printLower(19);
	}
	
	public void printUpper(int letter) {
		System.out.println(letter + 64);
	}
	
	public void printLower(int letter) {
		System.out.println(letter + 96);
	}
}
