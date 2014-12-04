class a {
	public static void main(String[] args) {
		list l = new list();
		l.n = 2;
		l.append(3).append(4).append(5).append(6).append(7);
		System.out.println(l.length());
		System.out.println(l.s.length());
	}
}

class list {
	public int n;
	public list s;
	
	public list append(int n) {
		if (s != null) {
			s.append(n);
		} else {
			s = new list();
			s.n = n;
		}
		return this;
	}
	
	public int length() {
		int l = 1;
		list c = this;
		while (c.s != null) {
			l = l+1;
			c = c.s;
		}
		return l;
	}
}