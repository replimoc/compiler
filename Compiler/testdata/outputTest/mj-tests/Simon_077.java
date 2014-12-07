class a {
	public static void main(String[] args) {
		new a().w();
	}
	
	public void w() {
		print(true);
		boolean[] q = new boolean[2];
		q[0]= true;
		print(q[0]);
		print(q[1]);
	}
	
	public void print(boolean b) {
		if (b) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
	}
}