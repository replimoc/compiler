class a {
	public static void main(String[] args) {
		int i = 1;
		while (i < 5) {
			int j = 1;
			while (j<3) {
				System.out.println(i*10 + j);
				j=j+1;
			}
			i = i+1;
		}
	}
}