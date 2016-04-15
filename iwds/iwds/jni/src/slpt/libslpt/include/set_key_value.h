#ifndef _SET_VALUE_H_
#define _SET_VALUE_H_

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <asm/errno.h>
#include <assert.h>
#include <common.h>

#ifdef __cplusplus
extern "C" {
#endif

enum {
	KEY_TYPE_CHAR,
	KEY_TYPE_INT,
	KEY_TYPE_STR,
	KEY_TYPE_MEM,

	/* KEEP IT TO LAST */
	KEY_TYPE_NUMS,
};

struct key_value_param {
	const char *key;
	unsigned int offset;
	unsigned int type;
	unsigned int mem_size;
};

static inline void *to_key_addr(void *head, const struct key_value_param *param) {
	return head + param->offset;
}

static inline void set_key_char(void *head, const struct key_value_param *param, char ch) {
	*(char *)to_key_addr(head, param) = ch;
}

static inline void set_key_int(void *head, const struct key_value_param *param, int val) {
	*(int *)to_key_addr(head, param) = val;
}

static inline void set_key_str(void *head, const struct key_value_param *param, const char *str) {
	strcpy(to_key_addr(head, param), str);
}

static inline void set_key_mem(void *head, const struct key_value_param *param, const void *valp) {
	memcpy(to_key_addr(head, param), valp, param->mem_size);
}

static inline void do_set_key_value(void *head, const struct key_value_param *param, const void *valp) {
	switch (param->type) {
	case KEY_TYPE_CHAR: set_key_char(head, param, *(char *)valp); break;
	case KEY_TYPE_INT: set_key_int(head, param, *(int *)valp); break;
	case KEY_TYPE_STR: set_key_str(head, param, valp); break;
	case KEY_TYPE_MEM: set_key_mem(head, param, valp); break;
	default: assert(0);
	}
}

static inline void print_key_value_param(const struct key_value_param *param) {
	pr_info("key_value: [%s] [%d] [%d]\n", param->key, param->type, param->offset);
}

static inline void print_key_value(void *head, const struct key_value_param *param) {
	print_key_value_param(param);
	switch (param->type) {
	case KEY_TYPE_CHAR: pr_info("key_value: ---> [%c]\n", *(char *)to_key_addr(head, param)); break;
	case KEY_TYPE_INT: pr_info("key_value: ---> [%d]\n", *(int *)to_key_addr(head, param)); break;
	case KEY_TYPE_STR: pr_info("key_value: ---> [%s]\n", (const char *)to_key_addr(head, param)); break;
	case KEY_TYPE_MEM: pr_info("key_value: ---> [%s]\n", (const char *)to_key_addr(head, param)); break;
	default: assert(0);
	}
}

extern const struct key_value_param *find_key_param
(const struct key_value_param *params, unsigned int len, const char *key);
extern const struct key_value_param *find_key_param_match_type
(const struct key_value_param *params, unsigned int len, const char *key, unsigned int type);
extern int set_key_value
(void *head, const char *key, const void *valp, const struct key_value_param *params, unsigned len);
extern int set_key_value_match_type
(void *head, const char *key, const void *valp, 
const struct key_value_param *params, unsigned len, unsigned int type);

extern void print_key_value_params(const struct key_value_param *params, unsigned int len);
extern void print_key_values(void *head, const struct key_value_param *params, unsigned int len);

#define KEY_VALUE_DEF(type_t, member, key_str, key_type)  \
{                                                         \
    .key = key_str,                                       \
    .offset = offsetof(type_t, member),                   \
    .type = key_type,                                     \
}

#define KEY_VALUE_MEM_DEF(type_t, member, key_str, key_size) \
{                                                          \
    .key = key_str,                                        \
    .offset = offsetof(type_t, member),                    \
    .type = key_type,                                      \
	.mem_size = key_size,                                  \
}

#ifdef __cplusplus
}
#endif
#endif /* _SET_VALUE_H_ */
