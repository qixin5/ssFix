/*
 * Copyright (c) 2004, DoodleProject
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * Neither the name of DoodleProject nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package net.sf.doodleproject.numerics4j.statistics.distribution;

import net.sf.doodleproject.numerics4j.exception.NumericException;
import net.sf.doodleproject.numerics4j.special.Beta;

/**
 * <p>
 * The F distribution (1).
 * </p>
 * 
 * <p>
 * References:
 * <ol>
 * <li>
 * Eric W. Weisstein. "F Distribution." From MathWorld--A
 * Wolfram Web Resource. <a target="_blank" 
 * href="http://mathworld.wolfram.com/F-Distribution.html">
 * http://mathworld.wolfram.com/F-Distribution.html</a>
 * </li>
 * </ol>
 * </p>
 * 
 * @version $Revision: 1.1 $ $Date: 2006/01/06 21:52:40 $
 */
public class FDistribution extends ContinuousDistribution {

    /** The denominator degrees of freedom. */
    private double denominatorDegreesOfFreedom;

    /** The numerator degrees of freedom. */
    private double numeratorDegreesOfFreedom;
    
    /**
     * Default constructor.  Numerator degrees of freedom and denominator
     * degrees of freedom are both set to 1.
     */
    public FDistribution() {
        this(1.0, 1.0);
    }
    
    /**
     * Create a distribution with the given numerator degrees of freedom and
     * denominator degrees of freedom.
     * @param dfn the numerator degrees of freedom.
     * @param dfd the denominator degrees of freedom.
     */
    public FDistribution(double dfn, double dfd) {
        super();
        setNumeratorDegreesOfFreedom(dfn);
        setDenominatorDegreesOfFreedom(dfd);
    }
    
    /**
     * The CDF for this distribution. This method returns P(X &lt; x).
     * @param x the value at which the CDF is evaluated.
     * @return CDF for this distribution. 
     * @throws NumericException if the cumulative probability can not be
     *         computed.
     */
    public double cumulativeProbability(double x) throws NumericException {
        double ret;
        if (Double.isNaN(x)) {
            ret = Double.NaN;
        } else if (x <= 0.0) {
            ret = 0.0;
        } else if (Double.isInfinite(x)) {
            ret = 1.0;
        } else {
            double n = getNumeratorDegreesOfFreedom();
            double m = getDenominatorDegreesOfFreedom();
            
            ret = Beta.regularizedBeta((n * x) / (m + n * x), 0.5 * n, 0.5 * m);
        }
        return ret;
    }
    
    /**
     * Access the denominator degrees of freedom.
     * @return the denominator degrees of freedom.
     */
    public double getDenominatorDegreesOfFreedom() {
        return denominatorDegreesOfFreedom;
    }
    
    /**
     * Access the numerator degrees of freedom.
     * @return the numerator degrees of freedom.
     */
    public double getNumeratorDegreesOfFreedom() {
        return numeratorDegreesOfFreedom;
    }
    
    /**
     * The inverse CDF for this distribution.  This method returns x such that,
     * P(X &lt; x) = p.
     * @param p the cumulative probability.
     * @return x
     * @throws NumericException if the inverse cumulative probability can not
     *         be computed.
     */
    public double inverseCumulativeProbability(double p)
        throws NumericException
    {
        double ret;
        
        if (p < 0.0 || p > 1.0 || Double.isNaN(p)) {
            ret = Double.NaN;
        } else if (p == 0.0) {
            ret = 0.0;
        } else if (p == 1.0) {
            ret = Double.POSITIVE_INFINITY;
        } else {
            ret = findInverseCumulativeProbability(p, 0.0, 10,
                Double.POSITIVE_INFINITY);
        }
        
        return ret;
    }
    
    /**
     * Modify the denominator degrees of freedom.
     * @param degreesOfFreedom the new denominator degrees of freedom.
     */
    public void setDenominatorDegreesOfFreedom(double degreesOfFreedom) {
        if (degreesOfFreedom <= 0.0 || Double.isNaN(degreesOfFreedom)) {
            throw new IllegalArgumentException(
                "degrees of freedom must be positive.");
        }
        this.denominatorDegreesOfFreedom = degreesOfFreedom;
    }
    
    /**
     * Modify the numerator degrees of freedom.
     * @param degreesOfFreedom the new numerator degrees of freedom.
     */
    public void setNumeratorDegreesOfFreedom(double degreesOfFreedom) {
        if (degreesOfFreedom <= 0.0 || Double.isNaN(degreesOfFreedom)) {
            throw new IllegalArgumentException(
                "degrees of freedom must be positive.");
        }
        this.numeratorDegreesOfFreedom = degreesOfFreedom;
    }
}
