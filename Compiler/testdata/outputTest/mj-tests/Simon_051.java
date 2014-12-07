class a {
	public a its_this;
	public int n;
	public static void main(String[] args) {
		new a().p();
	}
	
	public void p() {
		its_this = this;
		n = 7;
		System.out.println(its_this.its_this.its_this.its_this.n);
	}
}