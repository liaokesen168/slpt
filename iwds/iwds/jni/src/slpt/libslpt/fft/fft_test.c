#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <stddef.h>
#include <common.h>
#include <fifo_ring.h>
#include <fft.h>
#include <file_ops.h>
#include <assert.h>
#include <array_ring.h>
#include <sys/time.h>

struct fifo_ring *fifo;
struct array_ring *array;
unsigned int fft_is_busy = 0;
float *fr, *fi;
unsigned int buffer_offset = 0;
unsigned int fft_dst_len = 8192;
unsigned int fft_src_len = 1024;
unsigned int buffer_size = 8192 * 2;
unsigned int fs = 100;

void clear_floats(float *f, unsigned int size) {
	unsigned int i;
	
	for (i = 0; i < size; ++i) {
		f[i] = 0;
	}
}

void print_float_ri(float *fr, float *fi, unsigned int len) {
	unsigned int i;

	for (i = 0; i < len; ++i) {
		printf ("%f+%fi;\n", fr[i], fi[i]);
	}
}

void on_fft_data_event(struct array_ring *array) {
	float *data = array_ring_get(array, fft_dst_len);
	struct timeval start, end;
	struct timeval delta;

	if (!data) {
		return;
	}
	printf ("FFT started\n");

	fft_is_busy = 1;

	clear_floats(fi, fft_dst_len);
	clear_floats(fr, fft_dst_len);
	memcpy(fr, data, fft_dst_len * sizeof(*data));

	gettimeofday(&start, NULL);
	FFT(fr, fi, fft_dst_len, 0);
	gettimeofday(&end, NULL);
	
	timersub(&end, &start, &delta);
	printf ("time last: %ld %ld\n", delta.tv_sec, delta.tv_usec);

	print_float_ri(fr, fi, fft_dst_len);
	fft_is_busy = 1;
}

void on_heart_data_event(unsigned long heart_data) {
	array_ring_add(array, (float )heart_data);
	if (!fft_is_busy) {
		on_fft_data_event(array);
	}
}

int fft_test (int argc, char *argv[]) {
	char *file_data;
	char *path;
	unsigned int file_size;

	if (argc < 2) {
		pr_err("invalid args\n");
		return 0;
	}
	path = argv[1];

	file_data = load_file(path, NULL, 0, &file_size);
	if (!file_data) {
		pr_err("failed to read file: %s\n", path);
		return 0;
	}

	fr = malloc(sizeof(*fr) * fft_dst_len);
	fi = malloc(sizeof(*fi) * fft_dst_len);
	assert(fr && fi);
	clear_floats(fr, fft_dst_len);
	clear_floats(fi, fft_dst_len);

	fifo = alloc_fifo_ring(fft_dst_len * 2);
	assert(fifo);

	array = alloc_array_ring(fft_dst_len * 2, fft_dst_len >> 2, fft_dst_len >> 4);
	assert(array);

	{
		int nr;
		unsigned long heart_data;
		char *data = file_data;
		char *tmp;

		while ((unsigned int)(data - file_data) < file_size){
			nr = sscanf(data, "%lu\n", &heart_data);
			if (nr > 0)
				on_heart_data_event(heart_data);
			tmp = strchr(data, '\n');
			if (tmp == NULL) {
				pr_info("end of file\n");
				break;
			}
			data = tmp + 1;
		}
	}

	return 0;
}
