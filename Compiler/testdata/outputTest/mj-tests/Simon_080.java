class a {
	public static void main(String[] args) {
		new a().w();
	}
	
	public void w() {
		boolean t = true;
		boolean f = false;
		print(t == f);
		print(t == t);
		print(f == f);
		print(f == t);
		print(t != f);
		print(t != t);
		print(f != f);
		print(f != t);
		print(f == f == f);
		print(t != t != t);
	}
	
	public void print(boolean b) {
		if (b) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
	}
}