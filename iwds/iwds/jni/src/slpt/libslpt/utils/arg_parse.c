#include <common.h>
#include <asm/errno.h>
#include <string.h>
#include <arg_parse.h>

#define is_space_char(c) ((unsigned char)(c) <= 0x32)

static inline void *skip_space_char(const char *p) {
	for (; ;) {
		if (p[0] == '\0' || !is_space_char(p[0])) break; ++p;
		if (p[0] == '\0' || !is_space_char(p[0])) break; ++p;
		if (p[0] == '\0' || !is_space_char(p[0])) break; ++p;
		if (p[0] == '\0' || !is_space_char(p[0])) break; ++p;
	}

	if (p[0] == '\0')
		return NULL;
	return (void *)p;
}

static inline void *skip_nonspace_char(const char *p) {
	for (; ;) {
		if (p[0] == '\0' || is_space_char(p[0])) break; ++p;
		if (p[0] == '\0' || is_space_char(p[0])) break; ++p;
		if (p[0] == '\0' || is_space_char(p[0])) break; ++p;
		if (p[0] == '\0' || is_space_char(p[0])) break; ++p;
	}

	return (void *)p;
}

void arg_parse_init(struct arg_parse *ap, char *buffer) {
	unsigned int size;

	if (!ap || !buffer) {
		pr_err("%s: invalid args\n", __func__);
		assert(0);
	}

	size = strlen(buffer);
	ap->buffer = buffer;
	ap->cp = ap->buffer;
	ap->ep = ap->buffer + size;
	ap->sc = *(ap->cp);
}

const char *arg_parse_next(struct arg_parse *ap) {
	char *p = ap->cp;
	char *ep;

	*(ap->cp) = ap->sc;			/* start from last parse */

	p = skip_space_char(p);		/* find the start of a string */
	if (!p)
		return NULL;

	ep = skip_nonspace_char(p); /* find the end of a string */
	ap->sc = *ep; *ep = '\0';  /* make the end null byte */
	ap->cp = ep;			   /* save the sence of current parse */

	return p;					/* p is our string */
}

void arg_parse_destory(struct arg_parse *ap) {
	*(ap->cp) = ap->sc;
}
