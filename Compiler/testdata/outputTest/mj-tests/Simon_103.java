class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int i = 0;
		while (i < 11) {
			System.out.println(sqrt(i));
			i = i + 1;
		}
	}
	
	public int sqrt(int n) {
		int l = 0;
		int h = n+1;
		while (h-l > 1) {
			int m = (h+l)/2;
			if (m*m > n) h=m; else l=m;
		}
		return l;
	}
}