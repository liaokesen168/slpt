#ifndef _KEY_READER_H_
#define _KEY_READER_H_
#ifdef __cplusplus
extern "C" {
#endif

struct key_reader;

extern struct key_reader *alloc_key_reader(char *buffer, int size);

extern void free_key_reader(struct key_reader *reader);

extern int kr_read_char(struct key_reader *reader, char *val);

extern int kr_read_bool(struct key_reader *reader, char *val);

extern int kr_read_short(struct key_reader *reader, short *val);

extern int kr_read_int(struct key_reader *reader, int *val);

extern int kr_read_longlong(struct key_reader *reader, long long *val);

extern int kr_read_float(struct key_reader *reader, float *val);

extern int kr_read_double(struct key_reader *reader, double *val);

extern int kr_read_string(struct key_reader *reader, char *buffer, int size);
extern int kr_create_string(struct key_reader *reader, char **buffer_p);

extern int kr_read_char_array(struct key_reader *reader, char *array, int size);
extern int kr_create_char_array(struct key_reader *reader, char **array_p);

extern int kr_read_bool_array(struct key_reader *reader, char *array, int size);
extern int kr_create_bool_array(struct key_reader *reader, char **array_p);

extern int kr_read_short_array(struct key_reader *reader, short *array, int size);
extern int kr_create_short_array(struct key_reader *reader, short **array_p);

extern int kr_read_int_array(struct key_reader *reader, int *array, int size);
extern int kr_create_int_array(struct key_reader *reader, int **array_p);

extern int kr_read_longlong_array(struct key_reader *reader, long long *array, int size);
extern int kr_create_longlong_array(struct key_reader *reader, long long **array_p);

extern int kr_read_float_array(struct key_reader *reader, float *array, int size);
extern int kr_create_float_array(struct key_reader *reader, float **array_p);

extern int kr_read_double_array(struct key_reader *reader, double *array, int size);
extern int kr_create_double_array(struct key_reader *reader, double **array_p);

static inline int kr_read_uchar(struct key_reader *reader, unsigned char *val) {
	return kr_read_char(reader, (char *) val);
}

static inline int kr_read_ushort(struct key_reader *reader, unsigned short *val) {
	return kr_read_short(reader, (short *) val);
}

static inline int kr_read_uint(struct key_reader *reader, unsigned int *val) {
	return kr_read_int(reader, (int *) val);
}

static inline int kr_read_ulonglong(struct key_reader *reader, unsigned long long *val) {
	return kr_read_longlong(reader, (long long *) val);
}

static inline int kr_read_uchar_array(struct key_reader *reader, unsigned char *array, int size) {
	return kr_read_char_array(reader, (char *) array, size);
}
static inline int kr_create_uchar_array(struct key_reader *reader, unsigned char **array_p) {
	return kr_create_char_array(reader, (char **) array_p);
}

static inline int kr_read_ushort_array(struct key_reader *reader, unsigned short *array, int size) {
	return kr_read_short_array(reader, (short *) array, size);
}
static inline int kr_create_ushort_array(struct key_reader *reader, unsigned short **array_p) {
	return kr_create_short_array(reader, (short **) array_p);
}

static inline int kr_read_uint_array(struct key_reader *reader, unsigned int *array, int size) {
	return kr_read_int_array(reader, (int *) array, size);
}
static inline int kr_create_uint_array(struct key_reader *reader, unsigned int **array_p) {
	return kr_create_int_array(reader, (int **) array_p);
}

static inline int kr_read_ulonglong_array(struct key_reader *reader, unsigned long long *array, int size) {
	return kr_read_longlong_array(reader, (long long *) array, size);
}
static inline int kr_create_ulonglong_array(struct key_reader *reader, unsigned long long **array_p) {
	return kr_create_longlong_array(reader, (long long **) array_p);
}

#ifdef __cplusplus
}
#endif
#endif /* _KEY_READER_H_ */
