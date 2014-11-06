class C
{
	public boolean[][][] qux(int a, boolean b, int c, boolean d)
	{
		boolean rrrr = d;
		boolean[] rrr = new boolean[c];
		rrr[32] = rrrr;
		boolean[][] rr = new boolean[a][];
		rr[2] = rrr;
		boolean[][][] r = new boolean[2][][];
		r[1] = rr;
		return r;
	}
}

class B
{
	public int i;
	public C baz;
	
	public B add(B _)
	{
		i = _.i + i;
		return this;
	}
	
	public B[] getBar(B _)
	{
		B[] r = new B[i];
		int j = 0;
		while(j < i)
		{
			r[j].i = _.i;
			j = j + 1;
		}
		return r;
	}
	
}

class A
{
	public B[] bb;
	
	public B[] getBB() {return bb;}
	
	/* mj special: void arrays */
	public void[][][] setBB(B[] bbb)
	{
		bb = bbb;;;;;;;;;
		return new void[7][][];
	}
	
	public static void main(String[] args)
	{
		int x = 1;
		int y = x + !!7 = 5;
		A a = new A();
		a.realmain();
	}
	
	public void realmain()
	{
		println(five());
		math(2, 5, 13);
		logic(false, true, false);
		compare(5, 123);
		foo();
		return;
	}
	
	public void add(B b, B bb)
	{
		this.bb[0] = b.add(bb);
	}
	
	public int five()
	{
		return 1 + ((2 + 3) - 1);
	}
	
	public void math(int a, int b, int c)
	{
		println(-a);
		println(- - - -a);
		println(a + b + c);
		println(a - b - c);
		println(a * b * c);
		println(a / b / c);
		println(a % b % c);
		println(a + b * c);
		println(a * b + c);
	}
	
	public void logic(boolean a, boolean b, boolean c)
	{
		println(!a);
		println(a && b && c);
		println(a || b || c);
	}
	
	public void compare(int a, int b)
	{
		println(a < b);
		println(a <= b);
		println(a == b);
		println(a != b);
		println(a >= b);
		println(a > b);
	}
	
	public boolean foo()
	{
		B a = new B();
		return a.getBar(a)[4].baz.qux(42, false, 23, true)[1][2][3];
	}
	
	public void println(int i) {System.out.println(i);}
	public void println(boolean b) {System.out.println(b);}
}
