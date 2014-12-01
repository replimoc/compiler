class Main
{
	public int val;
	public int val2;

	public void setVal(int z)
	{
		val = z;
		this.val2 = 5;
	}

	public void n(int y)
	{
		y = y + 1;
		System.out.println(y);
	}

	public void m(int x)
	{
		int y = x + 5;
		System.out.println(x);
		n(y);
		this.n(x);
	}

	public static void main(String[] args) {
		Main obj = new Main();
		obj.m(10);
	}

}