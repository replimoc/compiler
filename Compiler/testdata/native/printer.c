#include <stdlib.h>
#include <stdio.h>

void*  __attribute__((sysv_abi))  StringUtils$create() {
	char *test = calloc(12, 1);
	sprintf(test, "Hallo\n");
	return test;
}

void  __attribute__((sysv_abi))  StringUtils$printStatic(void *pointer) {
	printf("static: %s", pointer);
}

void  __attribute__((sysv_abi))  String$print(void* this) {
	printf("nonStatic: %s", this);
}
