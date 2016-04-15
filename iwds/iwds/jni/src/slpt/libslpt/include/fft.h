#ifndef _FFT_H_
#define _FFT_H_
#ifdef __cplusplus
extern "C" {
#endif

static const double PI = 3.141592653589793;

int FFT(float *fr, float *fi, int n, int flag);
void FFTShift(float *x, float *y, int n);
int nextpow2(int m);
float angle(float a, float b);
int mean(float *fr, int n, float *fr_mean);
int median(float *fr, int n, float *fr_median);

#ifdef __cplusplus
}
#endif
#endif /* _FFT_H_ */
