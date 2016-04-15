#include <string.h>

/* To deal with str, instead the first c1 we found, change to c2 */
char *strctoc(char *s, char c1, char c2)
{
	char *p = strchr(s, c1);
	if (p)
		*p = c2;
	return p;
}

/**
 * strtail() - get the string tail
 * @front : the giving front string
 * @dst : the dst string to be get the tail.
 *     if @font is completely match the front of @dst,
 *     then the left string in @dst is the tail.
 *
 * @return_val : the pionter of string tail if find, else null.
 * */
void *strtail(const char *front, const char *dst) {
	unsigned int len = strlen(front);

	if (!strncmp(front, dst, len)) {
		return (void *)(dst + len);
	}

	return NULL;
}

