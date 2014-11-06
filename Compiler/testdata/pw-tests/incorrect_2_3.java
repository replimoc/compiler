class Shifter {
	public boolean logicShift;
    
	public int shiftRight(int toShift, int howFar) {
		/*no ternary operator*/
        int res = logicShift? toShift >>> howFar : toShift >>: howFar;
		return res;
	}
}