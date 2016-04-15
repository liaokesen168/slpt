#ifndef _SIN_COS_INT_H_
#define _SIN_COS_INT_H_
#ifdef __cplusplus
extern "C" {
#endif

extern int sin_cos_table[][2];

static inline int sin_int(int degrees) {
	return sin_cos_table[degrees][0];
}

static inline int cos_int(int degrees) {
	return sin_cos_table[degrees][1];
}

#define SIN_COS_DIVIDER 1000000

#ifdef __cplusplus
}
#endif
#endif /* _SIN_COS_INT_H_ */
