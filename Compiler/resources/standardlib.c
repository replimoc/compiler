#include <stdio.h>
#include <stdlib.h>

void __main();

void  __attribute__((sysv_abi))  print_int(int i) {
	printf("%d\n", i);
}


void*  __attribute__((sysv_abi))  calloc_proxy(size_t num, size_t size) {
	return calloc(num, size);
}

int main() {
	__main();
	return 0;
}
