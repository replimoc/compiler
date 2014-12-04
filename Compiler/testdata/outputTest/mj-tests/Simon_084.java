class a {
	public a n;
	public static void main(String[] args) {
		new a().w();
	}
	
	public void w() {
		boolean t = true;
		boolean f = false;
		print(t && t);
		print(t && f);
		print(f && f);
		print(t || t);
		print(t || f);
		print(f || f);
		print(3 > 4 || f);
		print(3 > 4 || 5!=2);
	}
	
	public void print(boolean b) {
		if (b) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
	}
}