package com.axiom.lib.math ;
/**
 * 2-D n-dimensional FFT * clint hastings  28 Mar 97
 * adapted from book - Numerical Recipes in C
 */

public abstract class FFT extends Object {
boolean FORWARD = true;
boolean REVERSE = false;
final int REAL = 1;
final int IMAG = 2;
// fourn parameters
// data: array [1..2*length]  of real,complex pairs
// nn : array[1..ndim] of fft length in each dimension, must be power of 2
//      For 2-D, nn[1] = width, nn[2] = height
// ndim: number of dimensions
// foward: true for forward fft, false for reverse
public static void compute(double data[], int nn[], boolean forward) {    int ndim = nn.length ;

	int i1, i2, i3, i2rev, i3rev;
	int ip1, ip2, ip3, ifp1, ifp2;
	int ibit, idim, k1, k2, n, nprev, nrem, ntot;
	double tempi, tempr, theta, wi, wpi, wpr, wr, wtemp, sign2pi;

	if (forward)
		sign2pi = 2 * Math.PI;
	else
		sign2pi = -2 * Math.PI;
	ntot = 1;
	for (idim=1; idim<=ndim; idim++)
		ntot *= nn[idim];
	nprev = 1;

	for (idim=ndim; idim >=1; idim--) {
		n = nn[idim];
		nrem = ntot / (n * nprev);
		ip1 = nprev << 1;
		ip2 = ip1 * n;
		ip3 = ip2 * nrem;
		i2rev = 1;
		for (i2=1; i2<=ip2; i2+=ip1) {	/* bit reversal */
			if  (i2 < i2rev) {
				for (i1=i2; i1<=i2+ip1-2; i1+=2) {
					for (i3=i1; i3<=ip3; i3+=ip2) {
						i3rev = i2rev + i3 - i2;
						tempr = data[i3];
						data[i3] = data[i3rev];
						data[i3rev] = tempr;
						tempr = data[i3+1];
						data[i3+1] = data[i3rev+1];
						data[i3rev+1] = tempr;
					} // for i3
				} // for i1
			} // if i2
			ibit = ip2 >> 1;
			while (ibit>=ip1 && i2rev>ibit) {
				i2rev -= ibit;
				ibit >>= 1;
			} // while ibit
			i2rev += ibit;
		} // for i2
		ifp1 = ip1;
		while (ifp1 < ip2) {
			ifp2 = ifp1 << 1;
			theta = sign2pi * ip1 / ifp2;
			wtemp = Math.sin(0.5*theta);
			wpr = -2.0*wtemp*wtemp;
			wpi = Math.sin(theta);
			wr = 1.0;
			wi = 0.0;
			for (i3=1; i3<=ifp1; i3+=ip1) {
				for (i1=i3; i1<=i3+ip1-2; i1+=2) {
					for (i2=i1; i2<=ip3; i2+=ifp2) {
						k1 = i2;
						k2 = k1 + ifp1;
						tempr = wr * data[k2] - wi * data[k2+1];
						tempi = wr * data[k2+1] + wi * data[k2];
						data[k2] = data[k1] - tempr;
						data[k2+1] = data[k1+1] - tempi;
						data[k1] += tempr;
						data[k1+1] += tempi;
					} // for i2
				} // for i1
				wr = (wtemp=wr)*wpr - wi*wpi + wr;
				wi = wi*wpr + wtemp*wpi + wi;
			} // for i3
			ifp1 = ifp2;
		} // while ifp1 < ip2
		nprev *= n;
	} // for idim
}  // fft2()

} // class FFT
