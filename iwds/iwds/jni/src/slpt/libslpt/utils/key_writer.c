#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <asm/errno.h>
#include <assert.h>

#include "key_writer.h"

#define DEFAULT_CAPACITY 512

struct key_writer {
	unsigned char *buffer;
	int offset;
	int size;
};

char *kw_get_buffer(struct key_writer *writer) {
	return (char *) writer->buffer;
}

int kw_get_size(struct key_writer *writer) {
	return writer->offset;
}

struct key_writer *alloc_key_writer(int capacity) {
	struct key_writer *writer;

	if (capacity < 1)
		capacity = 1;

	writer = malloc(sizeof(*writer));
	assert(writer != NULL);

	writer->buffer = malloc(capacity);
	assert(writer->buffer != NULL);

	writer->size = capacity;
	writer->offset = 0;

	return writer;
}

void free_key_writer(struct key_writer *writer) {
	free(writer->buffer);
	free(writer);
}

static void ensure_capacity(struct key_writer *writer, int need_size) {
	int new_size;
	unsigned char *buffer;

	if ((writer->size - writer->offset) >= need_size)
		return;

	new_size = (writer->size * 3) / 2;
	if ((new_size - writer->offset) < need_size)
		new_size = ((writer->size + need_size) * 3 ) / 2;

	buffer = realloc(writer->buffer, new_size);
	assert(buffer != NULL);

	writer->buffer = buffer;
	writer->size = new_size;
}

static inline void put_char(struct key_writer *writer, char ch) {
	writer->buffer[writer->offset++] = (unsigned char) ch;
}

void kw_write_char(struct key_writer *writer, char val) {
	ensure_capacity(writer, 1);

	put_char(writer, val);
}

void kw_write_bool(struct key_writer *writer, char val) {
	kw_write_char(writer, val != 0 ? 1 : 0);
}

void kw_write_short(struct key_writer *writer, short val) {
	ensure_capacity(writer, 2);

	put_char(writer, (val >> 0) & 0xff);
	put_char(writer, (val >> 8) & 0xff);
}

void kw_write_int(struct key_writer *writer, int val) {
	ensure_capacity(writer, 4);

	put_char(writer, (val >> 0) & 0xff);
	put_char(writer, (val >> 8) & 0xff);
	put_char(writer, (val >> 16) & 0xff);
	put_char(writer, (val >> 24) & 0xff);
}

void kw_write_longlong(struct key_writer *writer, long long val) {
	ensure_capacity(writer, 4);

	put_char(writer, (val >> 0) & 0xff);
	put_char(writer, (val >> 8) & 0xff);
	put_char(writer, (val >> 16) & 0xff);
	put_char(writer, (val >> 24) & 0xff);
	put_char(writer, (val >> 32) & 0xff);
	put_char(writer, (val >> 40) & 0xff);
	put_char(writer, (val >> 48) & 0xff);
	put_char(writer, (val >> 56) & 0xff);
}

void kw_write_float(struct key_writer *writer, float val) {
	int *tmp = (int *)&val;
	kw_write_int(writer, *tmp);
}

void kw_write_double(struct key_writer *writer, double val) {
	long long *tmp = (long long *)&val;
	kw_write_longlong(writer, *tmp);
}

void kw_write_string(struct key_writer *writer, const char *str) {
	kw_write_char_array(writer, str, strlen(str) + 1);
}

void kw_write_char_array(struct key_writer *writer, const char *array, int size) {
	ensure_capacity(writer, size);
	kw_write_int(writer, size);

	memcpy(writer->buffer + writer->offset, array, size);
	writer->offset += size;
}

void kw_write_bool_array(struct key_writer *writer, const char *array, int size) {
	int i;

	ensure_capacity(writer, size);
	kw_write_int(writer, size);

	for (i = 0; i < size; ++i) {
		kw_write_char(writer, array[i] != 0 ? 1 : 0);
	}
}

void kw_write_short_array(struct key_writer *writer, const short *array, int size) {
	int i;

	ensure_capacity(writer, size * 2);
	kw_write_int(writer, size);

	for (i = 0; i < size; ++i) {
		kw_write_short(writer, array[i]);
	}
}

void kw_write_int_array(struct key_writer *writer, const int *array, int size) {
	int i;

	ensure_capacity(writer, size * 4);
	kw_write_int(writer, size);

	for (i = 0; i < size; ++i) {
		kw_write_int(writer, array[i]);
	}
}

void kw_write_longlong_array(struct key_writer *writer, const long long *array, int size) {
	int i;

	ensure_capacity(writer, size * 8);
	kw_write_int(writer, size);

	for (i = 0; i < size; ++i) {
		kw_write_longlong(writer, array[i]);
	}
}

void kw_write_float_array(struct key_writer *writer, const float *array, int size) {
	int i;

	ensure_capacity(writer, size * 4);
	kw_write_int(writer, size);

	for (i = 0; i < size; ++i) {
		kw_write_int(writer, *((int *)&array[i]));
	}
}

void kw_write_double_array(struct key_writer *writer, const double *array, int size) {
	int i;

	ensure_capacity(writer, size * 8);
	kw_write_int(writer, size);

	for (i = 0; i < size; ++i) {
		kw_write_longlong(writer, *((long long *)&array[i]));
	}
}
