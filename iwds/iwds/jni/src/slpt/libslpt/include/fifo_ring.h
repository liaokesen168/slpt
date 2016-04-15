#ifndef _FIFO_RING_H_
#define _FIFO_RING_H_
#ifdef __cplusplus
extern "C" {
#endif

struct fifo_ring {
	unsigned long *data;
	unsigned int cur;
	unsigned int len;
	unsigned int top;
	unsigned int size;
};

static inline void fifo_ring_init(struct fifo_ring *fifo, unsigned long *data, unsigned int size) {
	fifo->data = data;
	fifo->size = size;
	fifo->cur = fifo->top = 0;
	fifo->len = 0;
}

static inline int fifo_ring_is_empty(struct fifo_ring *fifo) {
	return !fifo->len;
}

static inline int fifo_ring_size(struct fifo_ring *fifo) {
	return fifo->size;
}

static inline int fifo_ring_len(struct fifo_ring *fifo) {
	return fifo->len;
}

static inline void fifo_ring_add(struct fifo_ring *fifo, unsigned long value) {
	fifo->data[fifo->top] = value;
	if (++fifo->top == fifo->size)
		fifo->top = 0;
	if (++fifo->len > fifo->size) {
		fifo->len = fifo->size;
		if (++fifo->cur == fifo->size)
			fifo->cur = 0;
	}
}

static inline int fifo_ring_get(struct fifo_ring *fifo, unsigned long *valuep) {
	if (!fifo_ring_is_empty(fifo)) {
		*valuep = fifo->data[fifo->cur];
		if (++fifo->cur == fifo->size)
			fifo->cur = 0;
		fifo->len--;
		return 1;
	}
	return 0;
}

/* utils */
static inline struct fifo_ring *alloc_fifo_ring(unsigned int size) {
	struct fifo_ring *fifo;
	unsigned long *data;
	
	fifo = malloc(sizeof(*fifo));
	if (!fifo)
		return NULL;
	
	data = malloc(sizeof(*data) * size);
	if (!data) {
		free(fifo);
		return NULL;
	}

	fifo_ring_init(fifo, data, size);
	return fifo;
}

static inline void free_fifo_ring(struct fifo_ring *fifo) {
	free(fifo->data);
	free(fifo);
}

#ifdef __cplusplus
}
#endif
#endif /* _FIFO_RING_H_ */
