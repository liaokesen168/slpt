#include <set_key_value.h>

const struct key_value_param *find_key_param
(const struct key_value_param *params, unsigned int len, const char *key) {
	unsigned int i;

	for (i = 0; i < len; ++i) {
		if (!strcmp(params[i].key, key))
			return &params[i];
	}
	return NULL;
}

const struct key_value_param *find_key_param_match_type
(const struct key_value_param *params, unsigned int len, const char *key, unsigned int type) {
	unsigned int i;

	for (i = 0; i < len; ++i) {
		if (params[i].type == type && !strcmp(params[i].key, key))
			return &params[i];
	}
	return NULL;
}

int set_key_value
(void *head, const char *key, const void *valp, const struct key_value_param *params, unsigned len) {
	const struct key_value_param *param;

	param = find_key_param(params, len, key);
	if (param)
		do_set_key_value(head, param, valp);

	return param ? 0 : -1;
}

int set_key_value_match_type
(void *head, const char *key, const void *valp,
const struct key_value_param *params, unsigned len, unsigned int type) {
	const struct key_value_param *param;

	param = find_key_param_match_type(params, len, key, type);
	if (param)
		do_set_key_value(head, param, valp);

	return param ? 0 : -1;
}

void print_key_value_params(const struct key_value_param *params, unsigned int len) {
	unsigned int i;

	for (i = 0; i < len; ++i) {
		print_key_value_param(&params[i]);
	}
}

void print_key_values(void *head, const struct key_value_param *params, unsigned int len) {
	unsigned int i;

	for (i = 0; i < len; ++i) {
		print_key_value(head, &params[i]);
	}
}
