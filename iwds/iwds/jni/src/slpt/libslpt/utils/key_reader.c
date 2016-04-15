#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <asm/errno.h>
#include <assert.h>

#include "key_reader.h"

struct key_reader {
	unsigned char *buffer;
	int offset;
	int size;
};

struct key_reader *alloc_key_reader(char *buffer, int size) {
	struct key_reader *reader;

	if (buffer == NULL || size < 0)
		return NULL;

	reader = malloc(sizeof(*reader));
	assert(reader != NULL);

	reader->buffer = (unsigned char *)buffer;
	reader->offset = 0;
	reader->size = size;

	return reader;
}

void free_key_reader(struct key_reader *reader) {
	if (reader)
		free(reader);
}

static inline int check_capacity(struct key_reader *reader, int need_size) {
	return (reader->size - reader->offset) < need_size;
}

static inline unsigned char take_a_char(struct key_reader *reader) {
	return reader->buffer[reader->offset++];
}

#define take_a_lchar(reader) ((unsigned long long) take_a_char(reader))

#define ensure_capacity(reader, need_size)			\
	do {											\
		if (check_capacity((reader), (need_size)))	\
			return -EINVAL;							\
	} while (0)

#define ensure_array_capacity(reader, len, n)	\
	do {										\
		int l;									\
												\
		if (kr_read_int((reader), &l))			\
			return -EINVAL;						\
												\
		if (check_capacity((reader), l * (n)))	\
			return -EINVAL;						\
												\
		(len) = l;								\
	} while(0)

#define check_array_capacity(reader, len, n, size)	\
	do {											\
		ensure_array_capacity(reader, len, n);		\
		if ((len) > (size))							\
			return -EINVAL;							\
	} while (0)

#define check_malloc_array(reader, len, n, p)	\
	do {										\
		ensure_array_capacity(reader, len, n);	\
		(p) = malloc((len) * (n));				\
		if (!(p))								\
			return -ENOMEM;						\
	} while (0)

int  kr_read_char(struct key_reader *reader, char *val) {
	ensure_capacity(reader, 1);

	*val = (char) take_a_char(reader);

	return 0;
}

int kr_read_bool(struct key_reader *reader, char *val) {
	return kr_read_char(reader, val);
}

int kr_read_short(struct key_reader *reader, short *val) {
	ensure_capacity(reader, 2);

	*val = (short)
		(take_a_char(reader) << 0 |
		 take_a_char(reader) << 8);

	return 0;
}

int kr_read_int(struct key_reader *reader, int *val) {
	ensure_capacity(reader, 4);

	*val = (int)
		(take_a_char(reader) << 0  |
		 take_a_char(reader) << 8  |
		 take_a_char(reader) << 16 |
		 take_a_char(reader) << 24);

	return 0;
}

int kr_read_longlong(struct key_reader *reader, long long *val) {
	ensure_capacity(reader, 8);

	*val = (long long)
		(take_a_lchar(reader) << 0  |
		 take_a_lchar(reader) << 8  |
		 take_a_lchar(reader) << 16 |
		 take_a_lchar(reader) << 24 |
		 take_a_lchar(reader) << 32 |
		 take_a_lchar(reader) << 40 |
		 take_a_lchar(reader) << 48 |
		 take_a_lchar(reader) << 56);

	return 0;
}

int kr_read_float(struct key_reader *reader, float *val) {
	return kr_read_int(reader, (int *) val);
}

int kr_read_double(struct key_reader *reader, double *val) {
	return kr_read_longlong(reader, (long long *) val);
}

int kr_read_char_array(struct key_reader *reader, char *array, int size) {
	int len;

	check_array_capacity(reader, len, 1, size);

	memcpy(array, reader->buffer + reader->offset, len);
	reader->offset += len;

	return len;
}

int kr_create_char_array(struct key_reader *reader, char **array_p) {
	int len;
	char *array;

	check_malloc_array(reader, len, 1, array);

	memcpy(array, reader->buffer + reader->offset, len);
	reader->offset += len;

	*array_p = array;

	return len;
}

int kr_read_bool_array(struct key_reader *reader, char *array, int size) {
	return kr_read_char_array(reader, array, size);
}

int kr_create_bool_array(struct key_reader *reader, char **array_p) {
	return kr_create_char_array(reader, array_p);
}

int kr_read_short_array(struct key_reader *reader, short *array, int size) {
	int len, i;

	check_array_capacity(reader, len, 2, size);

	for (i = 0; i < len; ++i) {
		kr_read_short(reader, array + i);
	}

	return len;
}

int kr_create_short_array(struct key_reader *reader, short **array_p) {
	int len, i;
	short *array;

	check_malloc_array(reader, len, 2, array);

	for (i = 0; i < len; ++i) {
		kr_read_short(reader, array + i);
	}

	*array_p = array;

	return len;
}

int kr_read_int_array(struct key_reader *reader, int *array, int size) {
	int len, i;

	check_array_capacity(reader, len, 4, size);

	for (i = 0; i < len; ++i) {
		kr_read_int(reader, array + i);
	}

	return len;
}

int kr_create_int_array(struct key_reader *reader, int **array_p) {
	int len, i;
	int *array;

	check_malloc_array(reader, len, 4, array);

	for (i = 0; i < len; ++i) {
		kr_read_int(reader, array + i);
	}

	*array_p = array;

	return len;
}

int kr_read_longlong_array(struct key_reader *reader, long long *array, int size) {
	int len, i;

	check_array_capacity(reader, len, 8, size);

	for (i = 0; i < len; ++i) {
		kr_read_longlong(reader, array + i);
	}

	return len;
}

int kr_create_longlong_array(struct key_reader *reader, long long **array_p) {
	int len, i;
	long long *array;

	check_malloc_array(reader, len, 8, array);

	for (i = 0; i < len; ++i) {
		kr_read_longlong(reader, array + i);
	}

	*array_p = array;

	return len;
}

int kr_read_float_array(struct key_reader *reader, float *array, int size) {
	int len, i;

	check_array_capacity(reader, len, 4, size);

	for (i = 0; i < len; ++i) {
		kr_read_float(reader, array + i);
	}

	return len;
}

int kr_create_float_array(struct key_reader *reader, float **array_p) {
	int len, i;
	float *array;

	check_malloc_array(reader, len, 4, array);

	for (i = 0; i < len; ++i) {
		kr_read_float(reader, array + i);
	}

	*array_p = array;

	return len;
}

int kr_read_double_array(struct key_reader *reader, double *array, int size) {
	int len, i;

	check_array_capacity(reader, len, 8, size);

	for (i = 0; i < len; ++i) {
		kr_read_double(reader, array + i);
	}

	return len;
}

int kr_create_double_array(struct key_reader *reader, double **array_p) {
	int len, i;
	double *array;

	check_malloc_array(reader, len, 8, array);

	for (i = 0; i < len; ++i) {
		kr_read_double(reader, array + i);
	}

	*array_p = array;

	return len;
}

int kr_read_string(struct key_reader *reader, char *buffer, int size) {
	int len;

	check_array_capacity(reader, len, 1, size - 1);

	memcpy(buffer, reader->buffer + reader->offset, len);
	reader->offset += len;

	buffer[len] = '\0';

	return len + 1;
}

int kr_create_string(struct key_reader *reader, char **buffer_p) {
	int len;
	char *buffer;

	ensure_array_capacity(reader, len, 1);

	buffer = malloc(len + 1);
	if (!buffer)
		return -ENOMEM;

	memcpy(buffer, reader->buffer + reader->offset, len * 1);
	reader->offset += len;

	buffer[len] = '\0';
	*buffer_p = buffer;

	return len + 1;
}
