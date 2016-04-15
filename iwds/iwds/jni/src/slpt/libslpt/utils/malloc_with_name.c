#include <common.h>
#include <malloc.h>

void *malloc_with_name(unsigned int size, const char *name) {
	unsigned int len = strlen(name) + 1;
	char *p = malloc(size + len);
	if (p) {
		memcpy(p + size, name, len);
	}

	return p;
}
