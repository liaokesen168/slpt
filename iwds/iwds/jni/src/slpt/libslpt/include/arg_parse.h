#ifndef _ARG_PARSE_H_
#define _ARG_PARSE_H_
#ifdef __cplusplus
extern "C" {
#endif


struct arg_parse {
	char *buffer;
	char *ep;
	char sc;
	char *cp;
};

extern const char *arg_parse_next(struct arg_parse *ap);
extern void arg_parse_init(struct arg_parse *ap, char *buffer);
extern void arg_parse_destory(struct arg_parse *ap);

#ifdef __cplusplus
}
#endif
#endif /* _ARG_PARSE_H_ */
