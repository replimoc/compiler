class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		boolean[] primetest = sieve(11);
		int i = 1;
		while (i < 11) {
			if (primetest[i]) System.out.println(i);
			i = i + 1;
		}
	}
	
	public boolean[] sieve(int n) {
		boolean[] s = new boolean[n];
		int i = 2;
		while (i < n) {
			s[i] = true;
			i =i+1;
		}
		i = 2;
		while (i < n) {
			if (s[i]) {
				int j = i * i;
				while (j < n) {
					s[j] = false;
					j = j + i;
				}
			}
			i=i+1;
		}
		return s;
	}
}