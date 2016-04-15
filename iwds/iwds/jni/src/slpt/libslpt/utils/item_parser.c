#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <item_parser.h>

void init_item_parser(struct item_parser *parser, const char *src, int separator) {
	parser->src = src ? src : "";
	parser->separator = separator;
	parser->start = parser->src;
	parser->end = parser->src;
	parser->buffer = NULL;
	parser->last_len = 0;
}

void destory_item_parser(struct item_parser *parser) {
	if (parser == NULL)
		return;

	if (parser->buffer) {
		free(parser->buffer);
	}

	memset(parser, sizeof(*parser), 0);
}

const char *item_parser_next(struct item_parser *parser) {
	unsigned int len;

	if (parser->end == NULL)
		return NULL;

	parser->start = parser->end;

	parser->end = strchr(parser->start, parser->separator);
	if (parser->end == NULL)
		len = strlen(parser->start) + 1;
	else
		len = parser->end - parser->start + 1;

	if (len > parser->last_len) {
		if (parser->buffer != NULL)
			free(parser->buffer);
		parser->buffer = malloc(len);
		if (parser->buffer == NULL) {
			parser->last_len = 0;
			return NULL;
		}
		parser->last_len = len;
	}

	memcpy(parser->buffer, parser->start, len);
	parser->buffer[len - 1] = '\0';

	if (parser->end != NULL)
		parser->end += 1;

	return parser->buffer;
}
