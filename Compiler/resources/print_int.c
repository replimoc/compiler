#include <stdio.h>

void  __attribute__((sysv_abi))  print_int(int i) {
	printf("%d\n", i);
}
