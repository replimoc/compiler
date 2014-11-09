class Weird {
	public int getValue() {
		int a = 0;
		
		if ((((a < 0)) && (a > 0) || !(a == 0)) { /* Missing ')' */
				{ ;}
				return -1;
			} /* Missing '{' */
		}
		
		return a;
	}
}