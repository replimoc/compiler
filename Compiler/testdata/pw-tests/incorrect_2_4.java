/*method not in class declaration*/
public static void main(String[] args) {
	Shifter normalShifter;
	System.out.println(normalShifter.shiftLeft(42,10));
	System.out.println(normalShifter.shiftRight(1337,3));
	
	/* this should go wrong at last! */
	Shifter logicShifter(true);
	System.out.println(logicShifter.shiftLeft(42,10));
	System.out.println(logicShifter.shiftRight(1337,3));
	return 0;
}
