#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <common.h>
#include "fft.h"

int FFT(float *fr, float *fi, int n, int flag)
{
	int mp, arg, cntr, p1, p2;
	int i, j, a, b, k;
	float sign, pr, pi, harm, t;
	//   float ca[1024*2],sa[1024*2];
	float *ca, *sa;
	ca=(float *)calloc(n,sizeof(float));
	sa=(float *)calloc(n,sizeof(float));
	//flag = 0---fft; flag = 1---ifft

	assert(ca);
	assert(sa);

	j = 0;
	if ( flag != 0 )
	{
		sign = 1.0f;
		for ( i = 0; i <= n-1; ++i )
		{
			fr[i] = fr[i] / n;
			fi[i] = fi[i] / n;
		}
	}
	else
		sign = -1.0f;
	for ( i = 0; i <= n-2; ++i )
	{
		if ( i < j )
		{
			t = fr[i];
			fr[i] = fr[j];
			fr[j] = t;
			t = fi[i];
			fi[i] = fi[j];
			fi[j] = t;
		}
		k = n / 2;
		while ( k <= j )
		{
			j -= k;
			k /= 2;
		}
		j += k;
	}
	mp = 0;
	i = n;
	while ( i != 1 )
	{
		mp += 1;
		i /= 2;
	}
	harm = (float)(2* PI / n);
	for ( i = 0; i <= n - 1; ++i)
	{
		sa[i] = (float)(sign * sin(harm * i));
		ca[i] = (float)(cos(harm * i));
	}
	a = 2;
	b = 1;
	for ( cntr = 1; cntr <= mp; ++cntr )
	{
		p1 = n / a;
		p2 = 0;
		for ( k = 0; k <= b - 1; ++k)
		{
			i = k;
			while ( i < n )
			{
				arg = i + b;
				if ( k == 0 )
				{
					pr = fr[arg];
					pi = fi[arg];
				}
				else
				{
					pr = fr[arg] * ca[p2] - fi[arg] * sa[p2];
					pi = fr[arg] * sa[p2] + fi[arg] * ca[p2];
				}
				fr[arg] = fr[i] - pr;
				fi[arg] = fi[i] - pi;
				fr[i] += pr;
				fi[i] += pi;
				i += a;
			}
			p2 += p1;
		}
		a *= 2;
		b *= 2;
	}
	free( ca );
	free( sa );
	return(1);
}


void FFTShift(float *x, float *y, int n)
{
	int i;
	float *b,*c;
	b=(float *)calloc(n,sizeof(float));
	c=(float *)calloc(n,sizeof(float));
	for(i=0;i<n;i++)
	{
		b[i]=x[i];
		c[i]=y[i];
	}
	for(i=0;i<n/2;i++)
	{
		x[i]=b[i+(n+1)/2];
		y[i]=c[i+(n+1)/2];
	}
	for(i=n/2;i<n;i++)
	{
		x[i]=b[i-n/2];
		y[i]=c[i-n/2];
	}
	free(b);
	free(c);
	return ;
}

int nextpow2(int m)
{
	int n=1;
	float flag=(float)m/2;
	while (flag>1)
	{
		flag=flag/2;
		n++;
	}
	n=1<<n;
	return n;
}

float angle(float a, float b)
{
	float angle=0;
	if (a>0 && b>0)
		angle=(float)atan(b/a);
	else if (a>0 && b<0)
		angle=(float)atan(b/a);
	else if (a<0 && b>0)
		angle=(float)(atan(b/a)+PI);
	else if (a<0 && b<0)
		angle=(float)(atan(b/a)-PI);
	if (a==0 && b==0)
		angle=(float)0.0;
	else if(a==0 && b>0)
		angle=(float)PI;
	else if(a==0 && b<0)
		angle=(float)-PI;
	else if(a!=0 && b==0)
		angle=(float)0.0;

	return angle;
}

int mean(float *fr, int n, float *fr_mean)
{
	int i;
	float fr_sum;
	fr_sum=0.0;
	for (i=0;i<n;i++)
		fr_sum=fr_sum+fr[i];

	*fr_mean=fr_sum/n;
	return 1;
}


int median(float *fr, int n, float *fr_median)
{
	int i,j;
	float tem;
	for (j=0;j<n-1;j++)
	{
		for (i=0;i<n-1;i++)
		{
			if (fr[i]>fr[i+1])
			{
				tem=fr[i];fr[i]=fr[i+1];fr[i+1]=tem;
			}
		}
	}
	*fr_median=fr[(int)(n/2)];

	return 1;
}
