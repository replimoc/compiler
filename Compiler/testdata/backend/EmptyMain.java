class EmptyMain {

	public static void main(String[] args) {
    int a = 42;
    int b = 43;
    int c = (new EmptyMain()).test();
    int d = a + b + c;
    System.out.println(d);
	}
  
  public int test() {
    return -41;
  }
}