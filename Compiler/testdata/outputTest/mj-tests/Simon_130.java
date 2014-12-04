class a {
	public b next;
	public static void main(String[] args) {
		a x = new a();
		b y = new b();
		x.next = y;
		y.next = x;
		x.ping(4);
		x.ping(5);
	}
	
	public void ping(int ttl) {
		System.out.println(-2);
		System.out.println(ttl);
		if (ttl > 0) next.pong(ttl-1);
	}
}

class b {
	public a next;
	public void pong(int ttl) {
		System.out.println(-3);
		System.out.println(ttl);
		if (ttl > 0) next.ping(ttl-1);
	}
}