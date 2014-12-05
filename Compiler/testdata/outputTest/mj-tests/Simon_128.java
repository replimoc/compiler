class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		System.out.println(ack(0,0));
		System.out.println(ack(0,1));
		System.out.println(ack(0,2));
		System.out.println(ack(0,3));
		System.out.println(ack(0,4));
		System.out.println(ack(1,0));
		System.out.println(ack(1,1));
		System.out.println(ack(1,2));
		System.out.println(ack(1,3));
		System.out.println(ack(1,4));
		System.out.println(ack(2,0));
		System.out.println(ack(2,1));
		System.out.println(ack(2,2));
		System.out.println(ack(2,3));
		System.out.println(ack(2,4));
		System.out.println(ack(3,0));
		System.out.println(ack(3,1));
		System.out.println(ack(3,2));
		System.out.println(ack(3,3));
		System.out.println(ack(3,4));
		System.out.println(ack(4,0));
	}
	
	public int ack(int n, int m) {
		if (n==0) return m+1;
		if (m==0) return ack(n-1,1);
		return ack(n-1,ack(n,m-1));
	}
}