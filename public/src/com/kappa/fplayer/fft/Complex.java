
package com.kappa.fplayer.fft;

/**
 * Class for basic operation with complex numbers.
 * 
 * @author Vojtech Vasek
 */
public class Complex {
    
    private double real;
    private double imaginary;

    /**
     * Create new complex number with zero in both parts.
     */
    public Complex() {
        this(0.0, 0.0);
    }

    /**
     * Create new complex number with given parts.
     * 
     * @param real real part of the complex number
     * @param imaginary imaginary part of the complex number
     */
    public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }
    
    /**
     * Set this complex number to given value.
     * 
     * @param real real part of the complex number
     * @param imaginary imaginary part of the complex number
     */
    public void set(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    /**
     * Retrieve imaginary part of this complex number.
     * 
     * @return imaginary part of the complex number
     */
    public double getImaginary() {
        return imaginary;
    }
    
    /**
     * Retrieve real part of this complex number.
     * 
     * @return real part of the complex number
     */
    public double getReal() {
        return real;
    }

    /**
     * Set imaginary part of this complex number.
     * 
     * @param imaginary imaginary part of the complex number
     */
    public void setImaginary(double imaginary) {
        this.imaginary = imaginary;
    }
    
    /**
     * Set real part of this complex number.
     * 
     * @param real real part of the complex number
     */
    public void setReal(double real) {
        this.real = real;
    }
    
    /**
     * Performs an complex addition on given two complex numbers.
     * Parts are added separately.
     * 
     * @param c1 first complex number
     * @param c2 second complex number
     * @return result as a new complex number
     */
    public static Complex complexAdd(Complex c1, Complex c2) {
        return new Complex(c1.getReal()+c2.getReal(), c1.getImaginary()+c2.getImaginary());
    }
    
    /**
     * Performs an complex subtraction on given two complex numbers.
     * Parts are subtracted separately.
     * 
     * @param c1 first complex number
     * @param c2 second complex number
     * @return result as a new complex number
     */
    public static Complex complexSub(Complex c1, Complex c2) {
        return new Complex(c1.getReal()-c2.getReal(), c1.getImaginary()-c2.getImaginary());
    }
    
    /**
     * Performs an complex multiplicaion on given two complex numbers.
     * Standard complex multiplication.
     * 
     * @param c1 first complex number
     * @param c2 second complex number
     * @return result as a new complex number
     */
    public static Complex complexMult(Complex c1, Complex c2) {
        double re = c1.getReal()*c2.getReal() - c1.getImaginary()*c2.getImaginary();
        double im = c1.getReal()*c2.getImaginary() + c1.getImaginary()*c2.getReal();

        return new Complex(re, im);
    }
    
    /**
     * Translates polar coordinates to a complex number representation.
     * Complex z = x + iy, polar conversion: x = r*cos(fi), y = r*sin(fi).
     * Then absolute value of the new complex number is equal to given distance 'r'.
     * 
     * @param r a distance
     * @param fi an angle (in radians)
     * @return new complex number as representation of the polar coordinates
     */
    public static Complex polarToComplex(double r, double fi) {
        return new Complex(r*Math.cos(fi), r*Math.sin(fi));
    }
    
    /**
     * Return the magnitude of a given complex number.
     * Magnitude is defined as square root of the sum of its parts squared.
     * 
     * @param c complex number
     * @return magnitude of given complex number
     */
    public static double magnitude(Complex c) {
        return Math.sqrt(c.getReal()*c.getReal() + c.getImaginary()*c.getImaginary());
    }
    
    /**
     * Return the argument of a complex number (aka phase).
     * It is the angle of the radius with the positive real axis.
     * 
     * @param comp complex number to be analysed
     * @return phase (angle in radians)
     */
    public static double phase(Complex comp) {
        if (comp.getReal() > 0) {
            return Math.atan(comp.getImaginary()/comp.getReal());
        } else if (comp.getReal() < 0 && comp.getImaginary() >= 0) {
            return Math.atan(comp.getImaginary()/comp.getReal()) + Math.PI;
        } else if (comp.getReal() < 0 && comp.getImaginary() < 0) {
            return Math.atan(comp.getImaginary()/comp.getReal()) - Math.PI;
        } else if (comp.getReal() == 0 && comp.getImaginary() > 0) {
            return Math.PI/2.0;
        } else if (comp.getReal() == 0 && comp.getImaginary() < 0) {
            return -Math.PI/2.0;
        }
        return Double.NaN;
    }
    
    /**
     * Count decibel value from given complex number.
     * Plus 1f will make sure of positive numbers, -1f at the end is performed
     * for removal of a noise.
     * 
     * @param comp value after FFT processing
     * @return dB value
     */
    public static double decibelv1(Complex comp) {
        return 10.0 * Math.log((1f + (double)(magnitude(comp) / Math.log(10)))) - 1f;
    }
    
    /**
     * Another (this one is standard) method to count decibel value.
     * 
     * @param comp value after FFT processing
     * @return dB value
     */
    public static double decibelv2(Complex comp) {
        return 20.0 * Math.log10(magnitude(comp));
    }
    
    /**
     * Print array of complex numbers in human readable format.
     * 
     * @param carr input complex numbers
     */
    public static void printArray(Complex[] carr) {
        for (Complex c : carr) {
            System.out.print(c.getReal() + " + i" + c.getImaginary() + "; ");
        }
        System.out.println();
    }
}
