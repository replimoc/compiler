class a {
	public static void main(String[] args) {
		list l = new list();
		l.append(2).append(8).append(0).append(7).append(5).append(1);
		list l2 = l.quicksort(l);
		cell c = l2.first;
		while (c != null) {
			System.out.println(c.n);
			c = c.s;
		}
	}
}

class cell {
	public int n;
	public cell s;
}

class list {
	public cell first;
	public cell last;
		
	public list append(int n) {
		cell c = new cell();
		c.n = n;
		if (first == null) {
			first = c;
			last = c;
		} else {
			last.s = c;
			last = c;
		}
		return this;
	}
		
	public void concat(list l2) {
		cell c = l2.first;
		while (c != null) {
			append(c.n);
			c = c.s;
		}
	}
	
	public list quicksort(list l) {
		if (l.first == null || l.first.s == null) {
			return l;
		}
		cell c = l.first;
		int p = c.n;
		c = c.s;
		list l1 = new list();
		list l2 = new list();
		while (c != null) {
			if (c.n <= p) l1.append(c.n); else l2.append(c.n);
			c = c.s;
		}
		l1 = quicksort(l1);
		l1.append(p);
		l2 = quicksort(l2);
		l1.concat(l2);
		return l1;
	}
}