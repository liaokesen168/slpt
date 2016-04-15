#ifndef _KEY_WRITER_H_
#define _KEY_WRITER_H_
#ifdef __cplusplus
extern "C" {
#endif

struct key_writer;

extern struct key_writer *alloc_key_writer(int capacity);

extern char *kw_get_buffer(struct key_writer *writer);

extern int kw_get_size(struct key_writer *writer);

extern void free_key_writer(struct key_writer *writer);

extern void kw_write_char(struct key_writer *writer, char val);

extern void kw_write_bool(struct key_writer *writer, char val);

extern void kw_write_short(struct key_writer *writer, short val);

extern void kw_write_int(struct key_writer *writer, int val) ;

extern void kw_write_longlong(struct key_writer *writer, long long val);

extern void kw_write_float(struct key_writer *writer, float val);

extern void kw_write_double(struct key_writer *writer, double val);

extern void kw_write_string(struct key_writer *writer, const char *str);

extern void kw_write_char_array(struct key_writer *writer, const char *array, int size);

extern void kw_write_bool_array(struct key_writer *writer, const char *array, int size);

extern void kw_write_short_array(struct key_writer *writer, const short *array, int size);

extern void kw_write_int_array(struct key_writer *writer, const int *array, int size);

extern void kw_write_longlong_array(struct key_writer *writer, const long long *array, int size);

extern void kw_write_float_array(struct key_writer *writer, const float *array, int size);

extern void kw_write_double_array(struct key_writer *writer, const double *array, int size);

static inline void kw_write_uchar(struct key_writer *writer, unsigned char val) {
	kw_write_char(writer, (char) val);
}

static inline void kw_write_ushort(struct key_writer *writer, unsigned short val) {
	kw_write_short(writer, (short) val);
}

static inline void kw_write_uint(struct key_writer *writer, unsigned int val) {
	kw_write_int(writer, (int) val);
}

static inline void kw_write_ulonglong(struct key_writer *writer, unsigned long long val) {
	kw_write_longlong(writer, (long long) val);
}

static inline void kw_write_uchar_array(struct key_writer *writer, const unsigned char *array, int size) {
	kw_write_char_array(writer, (const char *) array, size);
}

static inline void kw_write_ushort_array(struct key_writer *writer, const unsigned short *array, int size) {
	kw_write_short_array(writer, (const short *) array, size);
}

static inline void kw_write_uint_array(struct key_writer *writer, const unsigned int *array, int size) {
	kw_write_int_array(writer, (const int *) array, size);
}

static inline void kw_write_ulonglong_array(struct key_writer *writer, const unsigned long long *array, int size) {
	kw_write_longlong_array(writer, (const long long *) array, size);
}

#ifdef __cplusplus
}
#endif

#endif /* _KEY_WRITER_H_ */
