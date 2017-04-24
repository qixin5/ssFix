/*
 * Project      : Commons
 * File         : NumberUtilities.java
 * Version      : 1.2.0
 * Author       : Cartapanis Alexandre <alexandre.cartapanis@macymed.fr>
 * Contributors :
 *
 * Copyright (c) 2005-2006 Macymed SARL - http://www.macymed.fr
 * 
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.macymed.commons.lang;

import java.math.BigDecimal;
import java.math.BigInteger;


/** 
 * <p>
 * Provides extra functionality for Java Number Classs.
 * </p>
 * @author <a href="mailto:alexandre.cartapanis@macymed.fr">Cartapanis Alexandre</a>
 * @version 1.2.0
 * @since Commons - Lang API 1.0
 */
public class NumberUtilities {

    /**
     * <p>
     * Forbids direct instanciation.
     * </p>
     */
    private NumberUtilities() {
        //nop
    }
    
    /**
     * <p>
     * Turns a string value into a java.lang.Number.
     * </p>
     * <p>
     * First, the value is examined for a type qualifier on the end (<code>'f','F','d','D','l','L'</code>). If it is found, it starts trying to create successively larger types from the type specified until one is found that can hold the value.
     * </p>
     * <p>
     * If a type specifier is not found, it will check for a decimal point and then try successively larger types from Integer to BigInteger and from Float to BigDecimal.
     * </p>
     * <p>
     * If the string starts with "0x" or "-0x", it will be interpreted as a hexadecimal integer.  Values with leading 0's will not be interpreted as octal.
     * </p>
     * @param _val A String containing a number.
     * @return <code>Number</code> - The number created from the string.
     * @throws NumberFormatException If the value cannot be converted.
     */
    public static final Number createNumber(String _val) throws NumberFormatException {
        if (_val == null) {
            return null;
        }
        if (_val.length() == 0) {
            throw new NumberFormatException(_val + " is not a valid number.");
        }
        if (_val.startsWith("--")) {
            return null;
        }
        if (_val.startsWith("0x") || _val.startsWith("-0x")) {
            return createInteger(_val);
        }
        char lastChar = _val.charAt(_val.length() - 1);
        String mantis;
        String decim;
        String exp;
        int decimPos = _val.indexOf('.');
        int expPos = _val.indexOf('e') + _val.indexOf('E') + 1;
        if (decimPos > -1) {
            if (expPos > -1) {
                if (expPos < decimPos) {
                    throw new NumberFormatException(_val + " is not a valid number.");
                }
                decim = _val.substring(decimPos + 1, expPos);
            } else {
                decim = _val.substring(decimPos + 1);
            }
            mantis = _val.substring(0, decimPos);
        } else {
            if (expPos > -1) {
                mantis = _val.substring(0, expPos);
            } else {
                mantis = _val;
            }
            decim = null;
        }
        if (!Character.isDigit(lastChar)) {
            if (expPos > -1 && expPos < _val.length() - 1) {
                exp = _val.substring(expPos + 1, _val.length() - 1);
            } else {
                exp = null;
            }
            String num = _val.substring(0, _val.length() - 1);
            boolean allZeros = isAllZeros(mantis) && isAllZeros(exp);
            switch (lastChar) {
            case 'l' :
                if (decim == null && exp == null && isDigits(num.substring(1)) && (num.charAt(0) == '-' || Character.isDigit(num.charAt(0)))) {
                    try {
                        return createLong(num);
                    } catch (NumberFormatException nfe) {
                        //nop
                    }
                    return createBigInteger(num);
                }
                throw new NumberFormatException(_val + " is not a valid number.");
            case 'L' :
                if (decim == null && exp == null && isDigits(num.substring(1)) && (num.charAt(0) == '-' || Character.isDigit(num.charAt(0)))) {
                    try {
                        return createLong(num);
                    } catch (NumberFormatException nfe) {
                        //nop
                    }
                    return createBigInteger(num);
                }
                throw new NumberFormatException(_val + " is not a valid number.");
            case 'f' :
                try {
                    Float f = NumberUtilities.createFloat(num);
                    if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                        return f;
                    }
                } catch (NumberFormatException nfe) {
                    //nop
                }
            case 'F' :
                try {
                    Float f = NumberUtilities.createFloat(num);
                    if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                        return f;
                    }
                } catch (NumberFormatException nfe) {
                    //nop
                }
            case 'd' :
                try {
                    Double d = NumberUtilities.createDouble(num);
                    if (!(d.isInfinite() || (d.floatValue() == 0.0D && !allZeros))) {
                        return d;
                    }
                } catch (NumberFormatException nfe) {
                    //nop
                }
                try {
                    return createBigDecimal(num);
                } catch (NumberFormatException e) {
                    //nop
                }
            case 'D' :
                try {
                    Double d = NumberUtilities.createDouble(num);
                    if (!(d.isInfinite() || (d.floatValue() == 0.0D && !allZeros))) {
                        return d;
                    }
                } catch (NumberFormatException nfe) {
                    //nop
                }
                try {
                    return createBigDecimal(num);
                } catch (NumberFormatException e) {
                    //nop
                }
            default :
                throw new NumberFormatException(_val + " is not a valid number.");
            }
        }
        if (expPos > -1 && expPos < _val.length() - 1) {
            exp = _val.substring(expPos + 1, _val.length());
        } else {
            exp = null;
        }
        if (decim == null && exp == null) {
            try {
                return createInteger(_val);
            } catch (NumberFormatException nfe) {
                //nop
            }
            try {
                return createLong(_val);
            } catch (NumberFormatException nfe) {
                //nop
            }
            return createBigInteger(_val);
            
        }
        boolean allZeros = isAllZeros(mantis) && isAllZeros(exp);
        try {
            Float f = createFloat(_val);
            if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                return f;
            }
        } catch (NumberFormatException nfe) {
            //nop
        }
        try {
            Double d = createDouble(_val);
            if (!(d.isInfinite() || (d.doubleValue() == 0.0D && !allZeros))) {
                return d;
            }
        } catch (NumberFormatException nfe) {
            //nop
        }
        return createBigDecimal(_val);
    }
    
    /**
     * <p>
     * Converts a String to a Float.
     * </p>
     * @param _val A String to convert.
     * @return <code>Float</code> - The converted float.
     */
    public static final Float createFloat(String _val) {
        return Float.valueOf(_val);
    }
    
    /**
     * <p>
     * Converts a String to a Double.
     * </p>
     * @param _val A String to convert.
     * @return <code>Double</code> - The converted Double.
     */
    public static final Double createDouble(String _val) {
        return Double.valueOf(_val);
    }
    
    /**
     * <p>
     * Convert a String to a Integer, handling hex and octal notations.
     * </p>
     * @param _val A String to convert.
     * @return <code>Integer</code> - The converted Integer.
     */
    public static final Integer createInteger(String _val) {
        return Integer.decode(_val);
    }
    
    /**
     * <p>
     * Converts a String to a Long.
     * </p>
     * @param _val A String to convert.
     * @return <code>Long</code> - The converted Long.
     */
    public static final Long createLong(String _val) {
        return Long.valueOf(_val);
    }
    
    /**
     * <p>
     * Converts a String to a BigInteger.
     * </p>
     * @param _val A String to convert.
     * @return <code>BigInteger</code> - The converted BigInteger.
     */
    public static final BigInteger createBigInteger(String _val) {
        BigInteger bi = new BigInteger(_val);
        return bi;
    }
    
    /**
     * <p>
     * Converts a String to a BigDecimal
     * </p>
     * @param _val A String to convert.
     * @return <code>BigDecimal</code> - The converted BigDecimal.
     */
    public static final BigDecimal createBigDecimal(String _val) {
        BigDecimal bd = new BigDecimal(_val);
        return bd;
    }
    
    /**
     * <p>
     * Checks whether the String contains only digit characters. Null and blank string will return false.
     * </p>
     * @param _str The string to check.
     * @return <code>boolean</code> - True if the string contains only unicode numeric, false otherwise.
     */
    public static final boolean isDigits(String _str) {
        if ((_str == null) || (_str.length() == 0)) {
            return false;
        }
        for (int i = 0, n = _str.length(); i < n; i++) {
            if (!Character.isDigit(_str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * <p>
     * Checks whether the String a valid Java number.
     * </p>
     * <p>
     * Valid numbers include hexadecimal marked with the "0x" qualifier, scientific notation and numbers marked with a type qualifier (e.g. 123L).
     * </p>
     * <p>
     * Null and blank string will return false.
     * </p>
     * @param _str the string to check.
     * @return <code>boolean</code> - True if the string is a correctly formatted number.
     */
    public static final boolean isNumber(String _str) {
        if ((_str == null) || (_str.length() == 0)) {
            return false;
        }
        char[] chars = _str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecimPt = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1) {
            if (chars[start] == '0' && chars[start + 1] == 'x') {
                int i = start + 2;
                if (i == sz) {
                    return false;
                }
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--;
        int i = start;
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;
            } else if (chars[i] == '.') {
                if (hasDecimPt || hasExp) {
                    return false;
                }
                hasDecimPt = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                if (hasExp) {
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false;
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                return false;
            }
            if (!allowSigns && (chars[i] == 'd' || chars[i] == 'D' || chars[i] == 'f' || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l' || chars[i] == 'L') {
                return foundDigit && !hasExp;
            }
        }
        return !allowSigns && foundDigit;
    }
    
    /**
     * <p>
     * Utility method for createNumber. Returns true if s is null.
     * </p>
     * @param _s The String to check.
     * @return <code>boolean</code> - True if it is all zeros or null, false otherwise.
     */
    private static final boolean isAllZeros(String _s) {
        if (_s == null) {
            return true;
        }
        for (int i = _s.length() - 1; i >= 0; i--) {
            if (_s.charAt(i) != '0') {
                return false;
            }
        }
        return _s.length() > 0;
    }
}
