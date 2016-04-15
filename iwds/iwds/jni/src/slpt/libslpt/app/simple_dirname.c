#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <asm/errno.h>

/* get file dir name
 * if your file is a dir, then it will be return it's parent dir
 * e.g     "/tmp/my_dir"   -->  "/tmp"
 *         "/tmp/my_dir/"   -->  "/tmp/my_dir"
 *         "./"    --> "./"
 */
const char *mdirname(char *fn) {
	char *p = fn;
	char *tmp;
	unsigned int cnt = 0;

	if (!strcmp(fn, "/")) {
		return fn;
	}

	while ((tmp = strchr(p, '/'))) {
		p = tmp + 1;
		cnt++;
	}

	if (cnt > 1) {
		*(p - 1) = '\0';
	} else {
		*(p) = '\0';
	}

	return fn;
}

int simple_dirname_main(int argc, char **argv) {
	if (argc != 2) {
		return 0;
	}

	printf ("%s\n", mdirname(argv[1]));

	return 0;
}
