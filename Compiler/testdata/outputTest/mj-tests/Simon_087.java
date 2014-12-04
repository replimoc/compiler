class a {
	public static void main(String[] args) {
		boolean b = false && new a().q();
		new a().print(b);
		b = true || new a().q();
		new a().print(b);
		b = false == (true || new a().q());
		new a().print(b);
		b = (true || new a().q()) == true;
		new a().print(b);
		boolean c;
		b = false || (c = (true || new a().q()) == true);
		new a().print(b);
		new a().print(c);
		new a().print(false && new a().q());
		System.out.println(3);
	}
	
	public boolean q() {
		System.out.println(-2);
		return true;
	}
	
	public void print(boolean b) {
		if (b) System.out.println(4); else System.out.println(5);
	}
}