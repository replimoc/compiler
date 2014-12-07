class Main
{
  public int member;
  
  public static void main(String[] args) {
    Main m = new Main();
    m.member = 0;
    m.method();
  }
  
  public void method() {
    System.out.println(member);
    
    member = member + 1;
    
    if (member >= 10) {
      return;
    }
    
    method();
  }
}