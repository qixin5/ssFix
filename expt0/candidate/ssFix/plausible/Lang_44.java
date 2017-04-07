/**
 *Copyright (c) 2002 Bright Side Factory.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Bright Side Factory (http://www.bs-factory.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Bright Side", "BS Factory" and "Bright Side Factory" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact info@bs-factory.org.
 *
 * 5. Products derived from this software may not be called "Bright Side",
 *    nor may "Bright Side" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Bright Side Factory.  For more
 * information on the Bright Side Factory, please see
 * <http://www.bs-factory.org/>.
 *
 */
package org.bsf.framework.commons.lang;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class Numbers {

    public Numbers() {
    }

    public static int stringToInt(String str) {
        return stringToInt(str, 0);
    }

    public static int stringToInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return def;
        }
    }

    public static Number createNumber(String val) throws NumberFormatException {
        if (val == null)
            return null;
        int idx = val.indexOf('.');
        if (idx != -1 && idx != val.length() - 1) {
            try {
                return createFloat(val);
            } catch (NumberFormatException nfe) {
            }
            try {
                return createDouble(val);
            } catch (NumberFormatException nfe) {
            }
            if (val.endsWith("f") || val.endsWith("F")) {
                String mant = val.substring(0, idx);
                String dec = val.substring(idx + 1, val.length() - 1);
                if (containsDigits(mant) && containsDigits(dec))
                    try {
                        return createFloat(val.substring(0, val.length() - 1));
                    } catch (NumberFormatException nfe) {
                    }
            }
            if (val.endsWith("d") || val.endsWith("D")) {
                String mant = val.substring(0, idx);
                String dec = val.substring(idx + 1, val.length() - 1);
                if (containsDigits(mant) && containsDigits(dec))
                    try {
                        return createDouble(val.substring(0, val.length() - 1));
                    } catch (NumberFormatException nfe) {
                    }
            }
            try {
                return createBigDecimal(val);
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("Unable to convert: " + val);
            }
        }
        try {
            return createInteger(val);
        } catch (NumberFormatException nfe) {
        }
        try {
            return createLong(val);
        } catch (NumberFormatException nfe) {
        }
        if ((val.endsWith("l") || val.endsWith("L")) && containsDigits(val.substring(0, val.length() - 1)))
            try {
                return createLong(val.substring(0, val.length() - 1));
            } catch (NumberFormatException nfe) {
            }
        try {
            return createBigInteger(val);
        } catch (NumberFormatException nfe) {
        }
        try {
            return Integer.valueOf(val, 16);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("Unable to convert: " + val);
        }
    }

    public static boolean containsDigits(String val) {
        if (val == null)
            return false;
        for (int i = 0; i < val.length(); i++)
            if (!Character.isDigit(val.charAt(i)))
                return false;

        return true;
    }

    public static Float createFloat(String val) {
        return Float.valueOf(val);
    }

    public static Double createDouble(String val) {
        return Double.valueOf(val);
    }

    public static Integer createInteger(String val) {
        return Integer.decode(val);
    }

    public static Long createLong(String val) {
        return Long.valueOf(val);
    }

    public static BigInteger createBigInteger(String val) {
        BigInteger bi = new BigInteger(val);
        return bi;
    }

    public static BigDecimal createBigDecimal(String val) {
        BigDecimal bd = new BigDecimal(val);
        return bd;
    }

    public static int minimum(int a, int b, int c) {
        if (b < a)
            a = b;
        if (c < a)
            a = c;
        return a;
    }

    public static boolean isNumber(String str) {
        char chrs[] = str.toCharArray();
        int sz = chrs.length;
        boolean decimal = false;
        for (int i = 0; i < sz; i++)
            if ((chrs[i] < '0' || chrs[i] > '9') && (i != 0 || chrs[i] != '-'))
                if (chrs[i] == '.' && !decimal)
                    decimal = true;
                else
                    return false;

        return true;
    }
}
