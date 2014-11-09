class Dangler {

	public int getValue()	/* Missing '{' */
		int a = 1;
		int b = 0;
  
		if (a == 1)
			if (b == 1)
				a = 42;
		else /* Belongs to the last if!!!! Indentation is wrong on purpose. */
			b = 42;
	
		return b;
	}
}
