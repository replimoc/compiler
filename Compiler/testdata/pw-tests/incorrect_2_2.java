class Shifter {
	public boolean logicShift;
    
	public int shiftLeft(int toShift, int howFar) {
	    /*no ternary operator, no <<<*/
        int res = logicShift? toShift <<< howFar : toShift << howFar;
		return res;
	}
}