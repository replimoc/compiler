class a {
	public static void main(String[] args) {
		new a().w();
	}
	
	public void w() {
		print(1<1);
		print(1>1);
		print(1>=1);
		print(1<=1);
		print(1<3);
		print(1>3);
		print(1>=3);
		print(1<=3);
		print(4<3);
		print(4>3);
		print(4>=3);
		print(4<=3);
	}
	
	public void print(boolean b) {
		if (b) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
	}
}