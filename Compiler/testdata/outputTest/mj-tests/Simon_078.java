class a {
	public static void main(String[] args) {
		new a().w();
	}
	
	public void w() {
		print(1!=1);
		int[] q = new int[1];
		print(q[0]==0);
		print(-1==1);
		print(1!=0);
	}
	
	public void print(boolean b) {
		if (b) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
	}
}