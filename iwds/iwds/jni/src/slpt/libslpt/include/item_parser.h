#ifndef _ITEM_PARSER_H_
#define _ITEM_PARSER_H_
#ifdef __cplusplus
extern "C" {
#endif

struct item_parser {
	const char *src;
	char *buffer;

	int separator;
	const char *start;
	const char *end;
	unsigned int last_len;
};

extern void init_item_parser(struct item_parser *parser, const char *src, int separator);

extern void destory_item_parser(struct item_parser *parser);

extern const char *item_parser_next(struct item_parser *parser);

#ifdef __cplusplus
extern "C" {
#endif
#endif /* _ITEM_PARSER_H_ */
