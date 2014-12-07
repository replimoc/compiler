class a {
	public static void main(String[] args) {
		list l = new list();
		l.n = 2;
		int i = 2;
		while (i < 11) {
			l.test(i);
			i=i+1;
		}
		while (l != null) {
			System.out.println(l.n);
			l = l.s;
		}
	}
}

class list {
	public int n;
	public list s;
	
	public void test(int n) {
		if (n % this.n == 0) {
			return;
		}
		if (s != null) {
			s.test(n);
		} else {
			s = new list();
			s.n = n;
		}
	}
}