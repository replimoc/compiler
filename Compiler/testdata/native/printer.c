#include <stdlib.h>
#include <stdio.h>

void* Pointer$create() {
	char *test = calloc(12, 1);
	sprintf(test, "Hallo");
	return test;
}

void Pointer$print(void *pointer) {
	printf("%s", pointer);
}
