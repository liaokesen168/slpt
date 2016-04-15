#ifndef _ARRAY_RING_H_
#define _ARRAY_RING_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <assert.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#if 1
#define array_ring_debug(x...)
#else
#define array_ring_debug pr_info
#endif

struct array_ring {
	float *data;
	unsigned int size;
	unsigned int len;
	unsigned int cur;
	unsigned int drop;
	unsigned int warn;
	unsigned int is_using;
};

static inline void array_ring_init(struct array_ring *array, float *data, unsigned int size,
								   unsigned int threshold, unsigned int drop) {
	assert((size / 2) >= drop  && (size / 2) >= threshold);
	array->data = data;
	array->size = size;
	array->cur = 0;
	array->len = 0;
	array->is_using = 0;
	array->drop = drop;
	array->warn = array->size - (threshold >= drop ? threshold : drop);
}

static inline unsigned int array_ring_len(struct array_ring *array) {
	return array->len;
}

static inline void array_ring_set_drop_threshold(struct array_ring *array,
												 unsigned int drop,
												 unsigned int threshold) {
	assert((array->size / 2) >= drop  && (array->size / 2) >= threshold);
	array->drop = drop;
	array->warn = array->size - (threshold >= drop ? threshold : drop);
}

static inline void array_ring_set_using(struct array_ring *array, int is_using) {
	array->is_using = !!is_using;
}

static inline void array_ring_rerange(struct array_ring *array) {
	memmove(&array->data[0], &array->data[array->cur], array->len * sizeof(array->data[0]));
	array->cur = 0;
}

static inline float *array_ring_get(struct array_ring *array, unsigned int len) {
	float *tmp;

	array_ring_set_using(array, 1);
	array_ring_debug("%s : ", __FUNCTION__);
	if (array->len >= len) {
		if ((array->cur + array->len) >= array->warn) {
			array_ring_debug("  rerange ");
			array_ring_rerange(array);
		}
		tmp = &array->data[array->cur];
		if (array->drop < len) {
			array_ring_debug("  drop1 ");
			array->len -= array->drop;
			array->cur += array->drop;
		} else {
			array_ring_debug("  drop2 ");
			array->len -= len;
			array->cur += len;
		}
		array_ring_debug("%d %d\n", array->cur, array->len);
		array_ring_debug("\n");
		return tmp;
	} else {
		array_ring_debug("lack len\n");
		return NULL;
	}
}

static inline void array_ring_add(struct array_ring *array, float value) {
	array_ring_debug("%s : ", __FUNCTION__);

	if ((array->cur + array->len) >= array->size) {
		array_ring_debug(" over threshold ");
		if (!array->is_using && (array->cur != 0)) {
			array_ring_rerange(array);
		} else {
			array->len = array->warn - array->cur;
		}
	}

	array_ring_debug("%d %d\n", array->cur, array->len);

	array->data[array->cur + array->len] = value;
	array->len += 1;
	array_ring_debug("\n");
}

/* utils */
static inline struct array_ring *alloc_array_ring(unsigned int data_size, unsigned int threshold, unsigned int drop) {
	unsigned int size;
	struct array_ring *array;
	float *data;

	size = data_size + threshold;
	array = malloc(sizeof(*array));
	if (!array)
		return NULL;

	data = malloc(sizeof(*data) * size);
	if (!data) {
		free(array);
		return NULL;
	}

	array_ring_init(array, data, size, threshold, drop);

	return array;
}

static inline void free_array_ring(struct array_ring *array) {
	free(array->data);
	free(array);
}

#ifdef __cplusplus
}
#endif
#endif /* _ARRAY_RING_H_ */
