class Shifter {

    public void method() {

	/* this should go wrong at last! */
        Shifter logicShifter (true);
        System.out.println(logicShifter.shiftLeft(42, 10));
        System.out.println(logicShifter.shiftRight(1337, 3));
        return 0;
    }
}
