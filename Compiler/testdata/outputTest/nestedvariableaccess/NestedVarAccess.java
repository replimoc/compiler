class Test {
	public static void main(String[] args) {
		int x=1;
		System.out.println(x);
		
    {
      x=2;
      System.out.println(x);
      int i = 50;
      System.out.println(i);
      i = i + x;
      System.out.println(i);
    }
    
		x = 42;
		System.out.println(x);
	}
}