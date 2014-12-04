class a {
	public static void main(String[] args) {
		int[] t = new int[4];
		t[2] = 1;
		t[1] = 0;
		t[0] = 3;
		t[3] = 42;
		System.out.println(t[2]);
		System.out.println(t[t[2]]);
		System.out.println(t[t[t[2]]]);
		System.out.println(t[t[t[t[2]]]]);
	}
}
