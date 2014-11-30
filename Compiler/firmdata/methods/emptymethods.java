class M
{
	public static void main(String[] args) {

	}
}

class A
{
	public void a1(){}
	public void a2(int x){}
	public void a3(boolean b){}
}

class B
{
	public void b1(A a){}
	public void b2(B b){}
}

class C
{
	public int c1() { return 0;}
	public boolean c2() { return false;}
	public A c3() { return null;}
	public C c4() { return null;}
	public D c5() { return null;}
}

class D
{
	public int d1(int x) {return 0;}
}