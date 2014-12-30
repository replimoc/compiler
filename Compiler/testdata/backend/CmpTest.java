class CmpTest {
	public static void main(String[] args) {
    int a = 0;
    int c = 1;

		if (a == a) {
			System.out.println(42);
		} else {
			System.out.println(43);
		}
    
    if (a == c) {
			System.out.println(42);
		} else {
			System.out.println(43);
		}
    
    if (a != a) {
			System.out.println(42);
		} else {
			System.out.println(43);
		}
    
    if (a > c) {
			System.out.println(42);
		} else {
			System.out.println(43);
		}
    
    if (c < a) {
			System.out.println(42);
		} else {
			System.out.println(43);
		}
    
    if (a <= a) {
			System.out.println(42);
		} else {
			System.out.println(43);
		}
    
    if (a >= a) {
			System.out.println(42);
		} else {
			System.out.println(43);
		}
	}
}