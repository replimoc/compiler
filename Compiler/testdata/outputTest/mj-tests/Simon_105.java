class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int i = 1;
		while (i < 11) {
			if (primetest(i)) System.out.println(i);
			i = i + 1;
		}
	}
	
	public int sqrt(int n) {
		int l = 0;
		int h = n;
		while (h-l > 1) {
			int m = (h+l)/2;
			if (m*m > n) h=m; else l=m;
		}
		return l;
	}
	
	public boolean primetest(int n) {
		if (n == 1) return false;
		int upper = sqrt(n);
		int i = 2;
		while (i <= upper) {
			if (n % i == 0) {
				return false;
			}
			i=i+1;
		}
		return true;
	}
}