class a {
	public static void main(String[] args) {
		Bool x = new Bool();
		if (x.b) {
			System.out.println(2);
		} else {
			System.out.println(1);
		}
		x.b = true;
		if (x.b) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
	}
}

class Bool {
	public boolean b;
}