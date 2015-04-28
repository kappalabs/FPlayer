
package com.kappa.fplayer.fft;

import java.util.Arrays;

/**
 * Class for performation of the Fast Fourier Transform algorithm.
 * Recursive function is used here. FFT expects array with length of power of two,
 * otherwise longer array is created and returned.
 * 
 * <p><a href="http://en.wikipedia.org/wiki/Window_function">Further window functions information</a>
 * 
 * @author Vojtech Vasek
 */
public class Transform {
    
    /**
     * Returns nearest power of integer 'base', which is bigger
     * or equal to given integer 'value'.
     * 
     * @param value value to be processed
     * @param base base of a number, whose power we are computing
     * @return nearest power of 'base' bigger or equal to 'value'
     */
    private static int getPow(int value, int base) {
        int nearest = 1;
        while (nearest < value) {
            nearest *= base;
        }
        return nearest;
    }

    /**
     * Counts Hamming window coefficients.
     * 
     * @param size length of the window
     * @param alpha standard alpha parameter
     * @param beta standard beta parameter
     * @return Hamming window coefficients
     */
    public static double[] hammingWindow(int size, double alpha, double beta) {
        double[] window = new double[size];
        for (int i=0; i < size; i++) {
            window[i] = alpha - beta * Math.cos((2.0 * Math.PI * i) / (size - 1));
        }
        
        return window;
    }
    
    /**
     * Counts Hanning/Hann window coefficients.
     * 
     * @param size length of the window
     * @return Hanning window coefficients
     */
    public static double[] hanningWindow(int size) {
        double[] window = new double[size];
        for (int i=0; i < size; i++) {
            window[i] = 0.5 * (1 - Math.cos((2.0 * Math.PI * i) / (size - 1)));
        }
        
        return window;
    }
    
    /**
     * Counts neutral rectangular window, which does not have any effects.
     * 
     * @param size length of the window
     * @return rectangular window coefficients (all ones)
     */
    public static double[] rectangularWindow(int size) {
        double[] window = new double[size];
        Arrays.fill(window, 1.0f);
        
        return window;
    }
    
    /**
     * Performs an application of window function on given complex array.
     * Parts of the arrays are multiplicated between themselves.
     * 
     * @param complIn complex data array, on which the window will be applied
     * @param window window function coefficients
     */
    public static void applyWindow(Complex[] complIn, double[] window) {
        for (int i=0; i < complIn.length && i < window.length; i++) {
            complIn[i] = new Complex(complIn[i].getReal() * window[i], 0);
        }
    }
    
    /**
     *  Recursively computes Fast Fourier Transform performed on input array 'ca'.
     *  Operates in O(N*log(N)), where N is length of the input array.
     * 
     * @param ca input array of complex numbers i.e. sound track
     * @return transformed array of complex numbers
     */
    private static Complex[] recFFT(Complex[] ca) {
        /*  Round the length of input array to the nearest power of 2 */
        int n = getPow(ca.length, 2);
        Complex[] cy = new Complex[n];

        if (n == 1) {
            cy[0] = ca[0];
            return cy;
        }

        Complex[] ca_s = new Complex[n/2];
        Complex[] ca_l = new Complex[n/2];
        Complex[] cy_s, cy_l;

        /*
         * Initialization of arrays:
         *   "ca_s" - elements with even index from array 'ca'
         *   "ca_l" - elements with odd index from array 'ca'
         */
        int i;
        for (i=0; i<n/2; i++) {
            ca_s[i] = ca[2*i];
            ca_l[i] = ca[2*i + 1];
        }

        /* Recursion ready to run */
        cy_s = recFFT(ca_s);
        cy_l = recFFT(ca_l);

        /* Use data computed in recursion */
        for (i=0; i < n/2; i++) {
            /* Multiply elements of 'cy_l' by the twiddle factors e^(-2*pi*i/N * k) */
            cy_l[i] = Complex.complexMult(Complex.polarToComplex(1, -2*Math.PI*i/n), cy_l[i]);
        }
        for (i=0; i < n/2; i++) {
            cy[i] = Complex.complexAdd(cy_s[i], cy_l[i]);
            cy[i + n/2] = Complex.complexSub(cy_s[i], cy_l[i]);
        }

        return cy;
    }
    
    /**
     * Perform FFT on given complex data array, return the result.
     * 
     * @param ca input complex data array
     * @return transformed complex data array
     */
    public static Complex[] transform(Complex[] ca) {
        return recFFT(ca);
    }
    
}
