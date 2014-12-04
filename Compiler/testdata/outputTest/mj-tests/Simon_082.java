class a {
	public static void main(String[] args) {
		new a().w();
	}
	
	public void w() {
		boolean x = true;
		print(!x);
		print(!false);
		print(!!true);
		print(!!false);
		print(!!!true);
		print(!!!false);
	}
	
	public void print(boolean b) {
		if (b) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
	}
}