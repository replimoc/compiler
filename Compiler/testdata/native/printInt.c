#include <stdio.h>

void  __attribute__((sysv_abi))  Test$print_int(int *streamdummy, int i) {
	printf("%d\n", i);
}
