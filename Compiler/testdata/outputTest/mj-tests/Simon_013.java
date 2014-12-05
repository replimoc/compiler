class a {
	public static void main(String[] args) {
		int a = 2;
		System.out.println((a=6)*a);
		a = 2;
		System.out.println(a*(a=6));
		System.out.println(a);
	}
}
