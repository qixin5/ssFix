/*
 * CharArrays.java
 *
 * Copyright (c) Jörg Waßmer
 * This library is free software. You can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 2 or above
 * as published by the Free Software Foundation.
 * For more information please visit <http://jaxlib.sourceforge.net>.
 */
package jaxlib.array;

import java.util.NoSuchElementException;
import java.util.Random;

import jaxlib.util.sorting.SortAlgorithm;
import jaxlib.jaxlib_private.CheckArg;
import jaxlib.jaxlib_private.CharTools;
import jaxlib.lang.Chars;



/**
 * Provides routines for working with <tt>char[]</tt> arrays.
 *
 * @author  <a href="mailto:joerg.wassmer@web.de">Jörg Waßmer</a>
 * @since   JaXLib 1.0
 * @version $Id: CharArrays.java,v 1.2 2004/12/07 21:55:30 joerg_wassmer Exp $
 */
public class CharArrays extends Object
{

  public static final char[] EMPTY_ARRAY = new char[0];

  static final int FILL_LIMIT             = 64;
  static final int NATIVE_ARRAYCOPY_LIMIT = 16;



  protected CharArrays() throws InstantiationException
  {
    throw new InstantiationException();
  }







  /**
   * @see #binarySearch(char[],int,int,char)
   *
   * @since JaXLib 1.0
   */
  public static int binarySearch(char[] data, char key) 
  {
    return binarySearch(data, 0, data.length, key);
  }
  
    
  /**
   * Searches the specified range of specified array for the specified value using the binary search algorithm.  
   * The array <strong>must</strong> be sorted (as by the <tt>sort</tt> method) prior to making this call.  If it
   * is not sorted, the results are undefined.  If the array contains multiple elements with the specified value, there 
   * is no guarantee which one will be found.
   *
   * @param   data      the array to be searched.
   * @param   fromIndex index of first element inclusive to search for.
   * @param   toIndex   index of last element exclusive to search for.
   * @param   key       the value to search for.
   *  
   * @return  index of the search key, if it is contained in the array; otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  
   *          The <i>insertion point</i> is defined as the point at which the key would be inserted into the array: 
   *          the index of the first element greater than the key, or <tt>array.length()</tt>, if all elements in the array in 
   *          specified range are less than the specified key.  Note that this guarantees that the return value will be &gt;= 0 if
   *          and only if the key is found.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value 
   *                                    (<tt>(fromIndex &lt; 0 || toIndex &gt; data.length || fromIndex &gt; toIndex)</tt>).
   * @throws NullPointerException       if <tt>data == null</tt>.
   *
   * @see java.util.Arrays#binarySearch(char[],char)
   *
   * @since JaXLib 1.0
   */
  public static int binarySearch(char[] data, int fromIndex, int toIndex, char key)
  {
    CheckArg.range(data.length, fromIndex, toIndex);
            
    --toIndex;
    while (fromIndex <= toIndex)
    {
      int mid = (fromIndex + toIndex) >> 1;

      char e = data[mid];
      if (key > e)
        fromIndex = mid + 1;
      else if (key < e)
        toIndex = mid - 1;
      else
        return mid; // key found
      
    }
    return ~fromIndex;

        
  }
  
  
  /**
   * Similar to <tt>binarySearch()</tt>, but guarantees to return the first index of searched element.
   *
   * @see #binarySearchLast(char[],int,int,char)
   *
   * @since JaXLib 1.0
   */
  public static int binarySearchFirst(char[] data, char key)  
  {
    return binarySearchFirst(data, 0, data.length, key);
  }
  
  
  /**
   * Similar to <tt>binarySearch()</tt>, but guarantees to return the first index of searched element.
   *
   * @see #binarySearchLast(char[],int,int,char)
   *
   * @since JaXLib 1.0
   */
  public static int binarySearchFirst(char[] data, int fromIndex, int toIndex, char key)
  {
    int high = binarySearch(data, fromIndex, toIndex, key); 
    if (high <= fromIndex)
      return high;

    int low    = fromIndex;      
    int result = high;
    high--;
      
    while (low <= high)
    {
      int mid = (low + high) >> 1;
      char e = data[mid];
      if (key > e)
        low = mid + 1;
      else if (key == e)
      {
        result = mid;
        low = fromIndex;
        high = mid - 1;
      }
      else
      {
        throw new IllegalArgumentException(
          "Array not properly sorted ascending between " + fromIndex + " and " + toIndex + ": " + data
        );
      }
    }

        
    return result;

  }


  
  /**
   * Similar to <tt>binarySearch()</tt>, but guarantees to return the last index of searched element.
   *
   * @see #binarySearchFirst(char[],int,int,char)
   *
   * @since JaXLib 1.0
   */
  public static int binarySearchLast(char[] data, char key) 
  {
    return binarySearchLast(data, 0, data.length, key);
  }
  
  
  /**
   * Similar to <tt>binarySearch()</tt>, but guarantees to return the last index of searched element.
   *
   * @see #binarySearchFirst(char[],int,int,char)
   *
   * @since JaXLib 1.0
   */
  public static int binarySearchLast(char[] data, int fromIndex, int toIndex, char key)
  {
    int low = binarySearch(data, fromIndex, toIndex, key);
    if (low < 0)
      return low;

    int result = low;
    low++;
    int high = toIndex - 1;
        
    while (low <= high)
    {
      int mid = (low + high) >> 1;
      char e = data[mid];
      if (key < e)
        high = mid - 1;
      else if (key == e)
      {
        result = mid;
        low = mid+1;
        high = toIndex-1;
      }
      else
      {
        throw new IllegalArgumentException(
          "Array not properly sorted ascending between " + fromIndex + " and " + toIndex + ": " + data
        );
      }
    }

        
    return result;

  }




  /**
   * Returns a new array which is equal to specified one.
   *
   * @throws NullPointerException       if <tt>src == null</tt>.
   *
   * @see #clone(char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static char[] clone(char[] src)
  {
    int remaining = src.length;
    if (remaining > NATIVE_ARRAYCOPY_LIMIT)
      return (char[]) src.clone();
    else
    {
      char[] a = new char[remaining];
      while (--remaining >= 0)
        a[remaining] = src[remaining];
      return a;
    }
  }
  
  
  /**
   * Returns a new array which is equal to the specified range of specified array.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value 
   *                                    (<tt>(fromIndex &lt; 0 || toIndex &gt; src.length || fromIndex &gt; toIndex)</tt>).
   * @throws NullPointerException       if <tt>src == null</tt>.
   *
   * @see #clone(char[])
   *
   * @since JaXLib 1.0
   */
  public static char[] clone(char[] src, int fromIndex, int toIndex)
  {
    CheckArg.range(src.length, fromIndex, toIndex);    
    char[] dest = new char[toIndex - fromIndex];
    copyFast(src, fromIndex, toIndex, dest, 0);
    return dest;
  }





  /**
   * Returns true if specified <tt>container</tt> array contains all elements of specified <tt>source</tt> array.
   * Same as <tt>containAll(source, 0, source.length, container, 0, container.length)</tt>.
   *
   * @param source    the array with the elements to check if they are contained in <tt>container</tt>.
   * @param container the array to check if it contains all the elements of the <tt>source</tt> array.
   *
   * @return <tt>true</tt> if all elements of <tt>source</tt> array are contained in <tt>container</tt> array.
   *
   * @throws NullPointerException       if <tt>source == null || container == null</tt>.
   *
   * @see #containsAll(char[],int,int,char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static boolean containsAll(char[] source, char[] container)
  {
    return containsAll(source, 0, source.length, container, 0, container.length);
  }

  
  /**
   * Returns true if specified range of specified <tt>container</tt> array contains all elements of specified range of specified <tt>source</tt> array.
   *
   * @param source          the array with the elements to check if they are contained in <tt>container</tt>.
   * @param sourceFromIndex index of first element in <tt>source</tt> array.
   * @param sourceToIndex   index after last element in <tt>source</tt> array.
   * @param container       the array to check if it contains all the elements of the <tt>source</tt> array.
   * @param fromIndex       index of first element in <tt>container</tt> array.
   * @param toIndex         index after last element in <tt>container</tt> array.
   *
   * @return <tt>true</tt> if all elements of specified range of <tt>source</tt> array are contained in specified range of <tt>container</tt> array.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value 
   *                                    (<tt>(fromIndex &lt; 0 || toIndex &gt; container.length || fromIndex &gt; toIndex)
   *                                    || (sourceFromIndex &lt; 0 || sourceToIndex &gt; source.length || sourceFromIndex &gt; sourceToIndex)</tt>).
   * @throws NullPointerException       if <tt>source == null || container == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static boolean containsAll(char[] source, int sourceFromIndex, int sourceToIndex, char[] container, int fromIndex, int toIndex)
  {
    Boolean fastShot = CheckArg.subListContainsSubList(source, source.length, sourceFromIndex, sourceToIndex, container, container.length, fromIndex, toIndex);
    if (fastShot != null)
      return fastShot == Boolean.TRUE ? true : false;
        
    final char first = source[sourceFromIndex++];      
    for (int i = fromIndex; i < toIndex; i++)
    {
      if (container[i] == first)
      {
        char p = first;
        WHILE: while (sourceFromIndex < sourceToIndex)
        {
          final char n = source[sourceFromIndex++];
          if ((n == p) || (n == first))
            continue WHILE;
          else
          {
            p = n;
            for (i = fromIndex; i < toIndex; i++)
            {
              if (container[i] == n)
                continue WHILE;
            }
          }
          return false;
        }
        return true;
      }
    }
    return false;


        
  }




  /**
   * Copies range of specified array to another position in the same array.
   *
   * @param data      the array.
   * @param fromIndex first index inclusive to copy.
   * @param toIndex   last index exclusive to copy.
   * @param destIndex the destination index where to copy elements between <tt>fromIndex</tt> and <tt>toIndex</tt> to.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value 
   *                                    (<tt>(fromIndex &lt; 0 || toIndex &gt; data.length || fromIndex &gt; toIndex || destIndex < 0 || destIndex ;gt data.length)</tt>),
   *                                    or if the range between <tt>destIndex</tt> and <tt>data.length</tt> is to small to hold
   *                                    the source range.
   * @throws NullPointerException       if <tt>data == null</tt>.
   *
   * @see #copy(char[],int,int,char[],int) for copying ranges between two different arrays.
   *
   * @since JaXLib 1.0
   */  
  public static void copy(char[] data, int fromIndex, int toIndex, int destIndex)
  {
    CheckArg.copyRangeIntern(data.length, fromIndex, toIndex, destIndex);
    copyFast(data, fromIndex, toIndex, destIndex);
  }



  /**
   * Same as <tt>copy()</tt>, but avoids range checking.
   * The behaviour of this method is undefined for illegal indices.
   *
   * @since JaXLib 1.0
   */
  public static void copyFast(char[] data, int fromIndex, int toIndex, int destIndex)
  {
    if ((toIndex - fromIndex) <= NATIVE_ARRAYCOPY_LIMIT)
    { 
      if (destIndex < fromIndex)
      {
        while (fromIndex < toIndex)
          data[destIndex++] = data[fromIndex++];
      }
      else
      {
        destIndex += (toIndex - fromIndex);
        while (fromIndex < toIndex)
          data[--destIndex] = data[--toIndex];
      }
    }
    else
      System.arraycopy(data, fromIndex, data, destIndex, toIndex - fromIndex); 
  }




  
  /**
   * Copies all elements of source array to destination array.
   * Source and destination indices are zero.
   *
   * @throws IndexOutOfBoundsException  if <tt>dest.length < src.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, char[] dest)
  {
    copy(src, 0, src.length, dest, 0);
  }

  /**
   * Copies all elements of source array starting with first element to destination array at specified index.
   *
   * @throws IndexOutOfBoundsException  if <tt>destIndex < 0 || destIndex > dest.length</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex + src.length > dest.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, char[] dest, int destIndex)
  {
    copy(src, 0, src.length, dest, destIndex);
  }


  /**
   * Copies elements of source array in specified range to destination array at specified destination index.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; src.length 
   *                                    || fromIndex &gt; toIndex)</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex < 0 || destIndex > dest.length</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex + (toIndex - fromIndex) > dest.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @see System#arraycopy
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, int fromIndex, int toIndex, char[] dest, int destIndex)
  {

    CheckArg.copyRangeTo(src.length, fromIndex, toIndex, dest.length, destIndex);
        
    if (src == dest)
    {
      copyFast(src, fromIndex, toIndex, destIndex);
      return;
    }
        
    while (fromIndex < toIndex)
      dest[destIndex++] = src[fromIndex++];

  }
  
  /**
   * Copies all elements of source array to destination array.
   * Source and destination indices are zero.
   *
   * @throws IndexOutOfBoundsException  if <tt>dest.length < src.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, double[] dest)
  {
    copy(src, 0, src.length, dest, 0);
  }

  /**
   * Copies all elements of source array starting with first element to destination array at specified index.
   *
   * @throws IndexOutOfBoundsException  if <tt>destIndex < 0 || destIndex > dest.length</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex + src.length > dest.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, double[] dest, int destIndex)
  {
    copy(src, 0, src.length, dest, destIndex);
  }


  /**
   * Copies elements of source array in specified range to destination array at specified destination index.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; src.length 
   *                                    || fromIndex &gt; toIndex)</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex < 0 || destIndex > dest.length</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex + (toIndex - fromIndex) > dest.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @see System#arraycopy
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, int fromIndex, int toIndex, double[] dest, int destIndex)
  {
    CheckArg.copyRangeTo(src.length, fromIndex, toIndex, dest.length, destIndex);
        
    while (fromIndex < toIndex)
      dest[destIndex++] = src[fromIndex++];
  }
  
  /**
   * Copies all elements of source array to destination array.
   * Source and destination indices are zero.
   *
   * @throws IndexOutOfBoundsException  if <tt>dest.length < src.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, float[] dest)
  {
    copy(src, 0, src.length, dest, 0);
  }

  /**
   * Copies all elements of source array starting with first element to destination array at specified index.
   *
   * @throws IndexOutOfBoundsException  if <tt>destIndex < 0 || destIndex > dest.length</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex + src.length > dest.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, float[] dest, int destIndex)
  {
    copy(src, 0, src.length, dest, destIndex);
  }


  /**
   * Copies elements of source array in specified range to destination array at specified destination index.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; src.length 
   *                                    || fromIndex &gt; toIndex)</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex < 0 || destIndex > dest.length</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex + (toIndex - fromIndex) > dest.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @see System#arraycopy
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, int fromIndex, int toIndex, float[] dest, int destIndex)
  {
    CheckArg.copyRangeTo(src.length, fromIndex, toIndex, dest.length, destIndex);
     
    while (fromIndex < toIndex)
      dest[destIndex++] = src[fromIndex++];
  }
  
  /**
   * Copies all elements of source array to destination array.
   * Source and destination indices are zero.
   *
   * @throws IndexOutOfBoundsException  if <tt>dest.length < src.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, int[] dest)
  {
    copy(src, 0, src.length, dest, 0);
  }

  /**
   * Copies all elements of source array starting with first element to destination array at specified index.
   *
   * @throws IndexOutOfBoundsException  if <tt>destIndex < 0 || destIndex > dest.length</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex + src.length > dest.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, int[] dest, int destIndex)
  {
    copy(src, 0, src.length, dest, destIndex);
  }


  /**
   * Copies elements of source array in specified range to destination array at specified destination index.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; src.length 
   *                                    || fromIndex &gt; toIndex)</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex < 0 || destIndex > dest.length</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex + (toIndex - fromIndex) > dest.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @see System#arraycopy
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, int fromIndex, int toIndex, int[] dest, int destIndex)
  {
    CheckArg.copyRangeTo(src.length, fromIndex, toIndex, dest.length, destIndex);
                   
    while (fromIndex < toIndex)
      dest[destIndex++] = src[fromIndex++];
  }
  
  /**
   * Copies all elements of source array to destination array.
   * Source and destination indices are zero.
   *
   * @throws IndexOutOfBoundsException  if <tt>dest.length < src.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, long[] dest)
  {
    copy(src, 0, src.length, dest, 0);
  }

  /**
   * Copies all elements of source array starting with first element to destination array at specified index.
   *
   * @throws IndexOutOfBoundsException  if <tt>destIndex < 0 || destIndex > dest.length</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex + src.length > dest.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, long[] dest, int destIndex)
  {
    copy(src, 0, src.length, dest, destIndex);
  }


  /**
   * Copies elements of source array in specified range to destination array at specified destination index.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; src.length 
   *                                    || fromIndex &gt; toIndex)</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex < 0 || destIndex > dest.length</tt>.
   * @throws IndexOutOfBoundsException  if <tt>destIndex + (toIndex - fromIndex) > dest.length</tt>.
   * @throws NullPointerException       if <tt>src == null || dest == null</tt>.
   *
   * @see System#arraycopy
   *
   * @since JaXLib 1.0
   */
  public static void copy(char[] src, int fromIndex, int toIndex, long[] dest, int destIndex)
  {
    CheckArg.copyRangeTo(src.length, fromIndex, toIndex, dest.length, destIndex);
                 
    while (fromIndex < toIndex)
      dest[destIndex++] = src[fromIndex++];
  }



  /**
   * Same as <tt>copy()</tt>, but avoids range check.
   * The behaviour of this method is undefined for illegal indices.
   *
   * @since JaXLib 1.0
   */
  public static void copyFast(char[] src, int fromIndex, int toIndex, char[] dest, int destIndex)
  {
    if (src == dest)
      copyFast(src, fromIndex, toIndex, destIndex);
    else if ((toIndex - fromIndex) <= NATIVE_ARRAYCOPY_LIMIT)
    {
      while (fromIndex < toIndex)
        dest[destIndex++] = src[fromIndex++];
    }
    else
      System.arraycopy(src, fromIndex, dest, destIndex, toIndex - fromIndex);
  }





  /**
   * @see #count(char[],int,int,int)
   *
   * @since JaxLib 1.00
   */
  public static int count(char[] a, int e)
  {
    return countUp(a, 0, a.length, e, -1);
  }

  /**
   * Counts each element in specified array which are equal to specified value.
   *
   * @param   data      The array to search in.
   * @param   fromIndex The first index to look in.
   * @param   toIndex   The lastIndex (exclusive) to look in.
   * @param   e         the value to search for.
   *
   * @return  Count of elements in array which are equal to <tt>e</tt>.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaxLib 1.00
   */
  public static int count(char[] data, int fromIndex, int toIndex, int e)
  {
    return countUp(data, fromIndex, toIndex, e, -1);
  }


  /**
   * @see #countUp(char[],int,int,int,int)
   *
   * @since JaxLib 1.00
   */
  public static int countUp(char[] data, int e, int maxCount)
  {
    return countUp(data, 0, data.length, e, maxCount);
  }

  /**
   * Counts a maximum amount of elements in specified array specified object is equal to.
   *
   * @param   data      The array to search in.
   * @param   fromIndex The first index to look in.
   * @param   toIndex   The lastIndex (exclusive) to look in.
   * @param   e         The element to count (Unicode code point).
   * @param   maxCount  The maximum count of elements to search for, or <tt>-1</tt> to search all.
   *
   * @return  Count of elements in array which are equal to <tt>e</tt>, but not more than <tt>maxCount</tt>.
   *
   * @throws  IllegalArgumentException    if <tt>maxCount &lt; -1</tt>.
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaxLib 1.00
   */
  public static int countUp(char[] data, int fromIndex, int toIndex, int e, int maxCount)
  {
    CheckArg.maxCount(maxCount);
    CheckArg.range(data.length, fromIndex, toIndex);
    if (maxCount == 0)
      return 0;
    
    int count = 0;
        
    if (e < Character.MIN_SUPPLEMENTARY_CODE_POINT)
    {
      while (fromIndex < toIndex)
      {
        if ((e == data[fromIndex++]) && (++count == maxCount))
          return count;
      }
    }
    else
    {
      while (fromIndex < toIndex)
      {
        if ((e == Character.codePointAt(data, fromIndex++)) && (++count == maxCount))
          return count;
      }
    }
        
    return count;
  }




  /**
   * Returns an array containing same elements in same order as specified one, but of length not less than specified minimum capacity.
   * 
   * @return  The same array as specified, if its <tt>length >= minCapacity</tt>, or a new one with <tt>length == minCapacity</tt>.
   *
   * @throws  IllegalArgumentException  if <tt>minCapacity < 0</tt>.
   * @throws  NullPointerException      if <tt>data == null</tt>.
   *
   * @see #ensureCapacity(char[],int,float)
   *
   * @since JaXLib 1.0
   */
  public static char[] ensureCapacity(char[] data, int minCapacity)
  {
    CheckArg.count(minCapacity);
    if (minCapacity > data.length)
    {
      char[] b = new char[minCapacity];
      copy(data, b);
      return b;
    }
    else
      return data;
  }

  /**
   * Returns an array containing same elements in same order as specified one, but of length not less than specified minimum capacity.
   * 
   * @return  The same array as specified, if its <tt>length >= minCapacity</tt>, or a new one with <tt>length == minCapacity + (minCapacity * growFactor)</tt>.
   *
   * @throws  IllegalArgumentException  if <tt>minCapacity < 0 || growFactor < 0</tt>.
   * @throws  NullPointerException      if <tt>data == null</tt>.
   *
   * @see #ensureCapacity(char[],int)
   *
   * @since JaXLib 1.0
   */
  public static char[] ensureCapacity(char[] data, int minCapacity, float growFactor)
  {
    CheckArg.count(minCapacity);
    if (growFactor < 0)
      throw new IllegalArgumentException("growFactor(" + growFactor + ") < 0.");
    if (minCapacity > data.length)
    {
      char[] b = new char[(int) (minCapacity + (minCapacity * growFactor))];
      copy(data, b);
      return b;
    }
    else
      return data;
  }
  
  
  /**
   * Returns <tt>true</tt> if the specified two arrays are identical, or are of equal length and containing equal elements in equal order.
   *
   * @see java.util.Arrays#equals(char[],char[]) 
   * @see #equals(char[],int,int,char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static boolean equals(char[] a, char[] b)
  {
    if (a == b)
      return true;
    if ((a == null) || (b == null))
      return false;
    int i = a.length;  
    if (b.length != i)
      return false;
     
    while(--i >= 0)
      if (a[i] != b[i])
        return false;
      
    return true;
  }

  
  /**
   * Checks if sequence in specified range of array <tt>a</tt> equals the sequence in specified range of array <tt>b</tt>.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>a == null || b == null</tt>.
   *
   * @see #equals(char[],char[])
   *
   * @since JaXLib 1.0
   */
  public static boolean equals(char[] a, int fromIndex, int toIndex, char[] b, int bFromIndex, int bToIndex)
  {
    CheckArg.range(a.length, fromIndex, toIndex);
    CheckArg.range(b.length, bFromIndex, bToIndex);
    if (toIndex - fromIndex != bToIndex - bFromIndex)
      return false;
    if (fromIndex == bFromIndex && toIndex == bToIndex && a == b)
      return true;
    
    while (fromIndex < toIndex)
      if (a[fromIndex++] != b[bFromIndex++])
        return false;
    return true;
  }
  
  

  public static int hashCode(char[] data)
  {
    return hashCode(data, 0, data.length);
  }

  public static int hashCode(char[] data, int fromIndex, int toIndex)
  {
    int hashCode = 1;
    while (fromIndex < toIndex)
      hashCode = (31 * hashCode) + jaxlib.lang.Chars.hashCode(data[fromIndex++]);
    return hashCode;
  }

  
  
  /**
   * @see #fill(char[],int,int,char)
   *
   * @since JaXLib 1.0
   */
  public static void fill(char[] data, char e)
  {
    fillFast(data, 0, data.length, e);
  }
  
  /**
   * Assigns the specified value to each element of the specified range of the specified array.  
   * The range to be filled extends from index <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.  
   * (If <tt>fromIndex==toIndex</tt>, the range to be filled is empty.)
   *
   * @param data      the array to be filled.
   * @param fromIndex the index of the first element (inclusive) to be filled with the specified value.
   * @param toIndex   the index of the last element (exclusive) to be filled with the specified value.
   * @param e         the value to be stored in all elements of the array in specified range.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void fill(char[] data, int fromIndex, int toIndex, char e)
  {
    CheckArg.range(data.length, fromIndex, toIndex);
    fillFast(data, fromIndex, toIndex, e);
  }
  
  /**
   * Same as <tt>fill()</tt>, but avoids range checking.
   * The behaviour of this method is undefined for illegal indices.
   *
   * @since JaXLib 1.0
   */
  public static void fillFast(char[] data, int fromIndex, int toIndex, char e)
  {
    if (toIndex - fromIndex < FILL_LIMIT)
    {
      while (fromIndex < toIndex)
        data[fromIndex++] = e;
    }
    else
    {
      int start = fromIndex;
      // fill first block, avoid native copy
      for (int hi = fromIndex + NATIVE_ARRAYCOPY_LIMIT; fromIndex < hi; fromIndex++)
        data[fromIndex] = e;
      int len = fromIndex - start;  
      // sequentially copy block using native copy; at each step the length of the block grows by 2.
      while (fromIndex < toIndex)
      {
        System.arraycopy(data, start, data, fromIndex, Math.min(len, toIndex - fromIndex));
        fromIndex += len;
        len <<= 1;
      }
    }
  }    




  /**
   * @see #indexOf(char[],int,int,char)
   *
   * @since JaXLib 1.0
   */
  public static int indexOf(char[] data, char e)
  {
    return indexOf(data, 0, data.length, e);
  }

  /**
   * @see #indexOf(char[],int,int,int)
   *
   * @since JaXLib 1.0
   */
  public static int indexOf(char[] data, int e)
  {
    return indexOf(data, 0, data.length, e);
  }

  
  /**
   * Returns the index in specified array of the first occurrence of the specified element, 
   * or <tt>-1</tt> if the array does not contain the element in specified range.
   *
   * @param data      the array to search in.
   * @param fromIndex the index of the first element (inclusive) to compare against <tt>e</tt>.
   * @param toIndex   the index of the last element (exclusive) to compare against <tt>e</tt>.
   * @param e         the element to search.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @see #lastIndexOf(char[],int,int,int)
   *
   * @since JaXLib 1.0
   */
  public static int indexOf(char[] data, int fromIndex, int toIndex, char e)
  {
    CheckArg.range(data.length, fromIndex, toIndex);
    
    while (fromIndex < toIndex)
    {
      if (e == data[fromIndex])
        return fromIndex;
      fromIndex++;
    }
        
    return -1;
  }
  
    
  /**
   * Returns the index in specified array of the first occurrence of the specified element, 
   * or <tt>-1</tt> if the array does not contain the element in specified range.
   *
   * @param data      the array to search in.
   * @param fromIndex the index of the first element (inclusive) to compare against <tt>e</tt>.
   * @param toIndex   the index of the last element (exclusive) to compare against <tt>e</tt>.
   * @param e         the element to search (Unicode code point).
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @see #lastIndexOf(char[],int,int,int)
   *
   * @since JaXLib 1.0
   */
  public static int indexOf(char[] data, int fromIndex, int toIndex, int e)
  {
    CheckArg.range(data.length, fromIndex, toIndex);
    
    if (e < Character.MIN_SUPPLEMENTARY_CODE_POINT)            
    {
      while (fromIndex < toIndex)
      {
        if (e == data[fromIndex])
          return fromIndex;
        fromIndex++;
      }
    }
    else
    {
      char hi = Chars.getHighSurrogate(e);
      char lo = Chars.getLowSurrogate(e);
      toIndex--;
      while (fromIndex < toIndex)
      {
        if ((hi == data[fromIndex]) && (lo == data[++fromIndex]))
          return fromIndex - 1;
      }
    }
        
    return -1;
  }


  public static int indexOf(char[] data, char e, boolean ignoreCase)
  {
    return indexOf(data, 0, data.length, e, ignoreCase);
  }
  
  public static int indexOf(char[] data, int e, boolean ignoreCase)
  {
    return indexOf(data, 0, data.length, e, ignoreCase);
  }

  
  /**
   * Searches optionally case insensitive the index in specified array of the first occurrence of the specified element, or returns <tt>-1</tt> if the array does not contain
   * the element in specified range.
   *
   * @param data       the array to search in.
   * @param fromIndex  the index of the most left element (inclusive) to compare against <tt>e</tt>.
   * @param toIndex    the index of the most right element (exclusive) to compare against <tt>e</tt>.
   * @param e          the element to search.
   * @param ignoreCase <code>true</code> to ignore case, false to search casesensitive.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length || fromIndex &gt; toIndex)</tt>.
   * @throws NullPointerException       if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static int indexOf(final char[] data, int fromIndex, int toIndex, final char e, final boolean ignoreCase)
  {
    if (!ignoreCase)
    {
      return indexOf(data, fromIndex, toIndex, e);
    }
    else
    {
      CheckArg.range(data.length, fromIndex, toIndex);
      final char eu = Character.toUpperCase(e);
      final char el = Character.toLowerCase(eu);

      while (fromIndex < toIndex)
      {
        if (CharTools.equalsIgnoreCase(data[fromIndex], el, eu))
        {
          return fromIndex;
        }
        fromIndex++;
      }
      return -1;
    }
  }
  
  
  /**
   * Searches optionally case insensitive the index in specified array of the first occurrence of the specified element, or returns <tt>-1</tt> if the array does not contain
   * the element in specified range.
   *
   * @param data       the array to search in.
   * @param fromIndex  the index of the most left element (inclusive) to compare against <tt>e</tt>.
   * @param toIndex    the index of the most right element (exclusive) to compare against <tt>e</tt>.
   * @param e          the element to search (Unicode code point).
   * @param ignoreCase <code>true</code> to ignore case, false to search casesensitive.
   *
   * @throws IndexOutOfBoundsException  for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length || fromIndex &gt; toIndex)</tt>.
   * @throws NullPointerException       if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static int indexOf(final char[] data, int fromIndex, int toIndex, final int e, final boolean ignoreCase)
  {
    if (!ignoreCase)
    {
      return indexOf(data, fromIndex, toIndex, e);
    }
    else
    {
      CheckArg.range(data.length, fromIndex, toIndex);
      final int eu = Character.toUpperCase(e);
      final int el = Character.toLowerCase(eu);

      if ((eu < Character.MIN_SUPPLEMENTARY_CODE_POINT) && (el < Character.MIN_SUPPLEMENTARY_CODE_POINT))
      {
        while (fromIndex < toIndex)
        {
          if (CharTools.equalsIgnoreCase(data[fromIndex], el, eu))
          {
            return fromIndex;
          }
          fromIndex++;
        }
        return -1;
      }
      else
      {
        while (fromIndex < toIndex)
        {
          if (CharTools.equalsIgnoreCase(Character.codePointAt(data, fromIndex), el, eu))
          {
            return fromIndex;
          }
          fromIndex++;
        }
        return -1;
      }      
    }
  }
  
  

  /**
   * @see #lastIndexOf(char[],int,int,char)
   *
   * @since JaXLib 1.0
   */
  public static int lastIndexOf(char[] data, char e)
  {
    return lastIndexOf(data, 0, data.length, e);
  }

  /**
   * @see #lastIndexOf(char[],int,int,int)
   *
   * @since JaXLib 1.0
   */
  public static int lastIndexOf(char[] data, int e)
  {
    return lastIndexOf(data, 0, data.length, e);
  }

  
  /**
   * Returns the index in specified array of the last occurrence of the specified element, or <tt>-1</tt> if the array does not contain
   * the element in specified range.
   *
   * @param data      the array to search in.
   * @param fromIndex the index of the most left element (inclusive) to compare against <tt>e</tt>.
   * @param toIndex   the index of the most right element (exclusive) to compare against <tt>e</tt>.
   * @param e         the element to search.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static int lastIndexOf(char[] data, int fromIndex, int toIndex, char e)
  {
    CheckArg.range(data.length, fromIndex, toIndex);
    
    while (fromIndex <= --toIndex)
    {
      if (e == data[toIndex])
        return toIndex;
    }
        
    return -1;
  }
  

  /**
   * Returns the index in specified array of the last occurrence of the specified element, or <tt>-1</tt> if the array does not contain
   * the element in specified range.
   *
   * @param data      the array to search in.
   * @param fromIndex the index of the most left element (inclusive) to compare against <tt>e</tt>.
   * @param toIndex   the index of the most right element (exclusive) to compare against <tt>e</tt>.
   * @param e         the element to search (Unicode code point).
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static int lastIndexOf(char[] data, int fromIndex, int toIndex, int e)
  {
    CheckArg.range(data.length, fromIndex, toIndex);
    
    if (e < Character.MIN_SUPPLEMENTARY_CODE_POINT)            
    {
      while (fromIndex <= --toIndex)
      {
        if (e == data[toIndex])
          return toIndex;
      }
    }
    else
    {
      char hi = Chars.getHighSurrogate(e);
      char lo = Chars.getLowSurrogate(e);
      while (fromIndex < --toIndex)
      {
        if ((hi == data[toIndex - 1]) && (lo == data[toIndex]))
          return toIndex - 1;
      }
    }
        
    return -1;
  }
  

  public static int lastIndexOf(char[] data, char e, boolean ignoreCase)
  {
    return lastIndexOf(data, 0, data.length, e, ignoreCase);
  }
  
  public static int lastIndexOf(char[] data, int e, boolean ignoreCase)
  {
    return lastIndexOf(data, 0, data.length, e, ignoreCase);
  }

  
  /**
   * Searches optionally case insensitive the index in specified array of the last occurrence of the specified element, or returns <tt>-1</tt> if the array does not contain
   * the element in specified range.
   *
   * @param data       the array to search in.
   * @param fromIndex  the index of the most left element (inclusive) to compare against <tt>e</tt>.
   * @param toIndex    the index of the most right element (exclusive) to compare against <tt>e</tt>.
   * @param e          the element to search.
   * @param ignoreCase <code>true</code> to ignore case, false to search casesensitive.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static int lastIndexOf(final char[] data, int fromIndex, int toIndex, final char e, final boolean ignoreCase)
  {
    if (!ignoreCase || (fromIndex == toIndex))
    {
      return lastIndexOf(data, fromIndex, toIndex, e);
    }
    else
    {
      CheckArg.range(data.length, fromIndex, toIndex);
      final char eu = Character.toUpperCase(e);
      final char el = Character.toLowerCase(eu);

      while (fromIndex < --toIndex)
      {
        if (CharTools.equalsIgnoreCase(data[toIndex], el, eu))
          return toIndex;
      }
      return -1;
    }
  }
  
  
  /**
   * Searches optionally case insensitive the index in specified array of the last occurrence of the specified element, or returns <tt>-1</tt> if the array does not contain
   * the element in specified range.
   *
   * @param data       the array to search in.
   * @param fromIndex  the index of the most left element (inclusive) to compare against <tt>e</tt>.
   * @param toIndex    the index of the most right element (exclusive) to compare against <tt>e</tt>.
   * @param e          the element to search (Unicode code point).
   * @param ignoreCase <code>true</code> to ignore case, false to search casesensitive.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static int lastIndexOf(final char[] data, int fromIndex, int toIndex, final int e, final boolean ignoreCase)
  {
    if (!ignoreCase || (fromIndex == toIndex))
    {
      return lastIndexOf(data, fromIndex, toIndex, e);
    }
    else
    {
      CheckArg.range(data.length, fromIndex, toIndex);
      final int eu = Character.toUpperCase(e);
      final int el = Character.toLowerCase(eu);

      if ((eu < Character.MIN_SUPPLEMENTARY_CODE_POINT) && (el < Character.MIN_SUPPLEMENTARY_CODE_POINT))
      {
        while (fromIndex < --toIndex)
        {
          if (CharTools.equalsIgnoreCase(data[toIndex], el, eu))
            return toIndex;
        }
        return -1;
      }
      else
      {
        while (fromIndex < --toIndex)
        {
          if (CharTools.equalsIgnoreCase(Character.codePointAt(data, toIndex), el, eu))
            return toIndex;
        }
        return -1;
      }      
    }
  }




  /**
   * @see #indexOfMax(char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static int indexOfMax(char[] data)
  {
    return indexOfMax(data, 0, data.length);
  }
  

  /**
   * Returns the index of the greatest element of specified range in specified array.
   *
   * @param data      the array to search in.
   * @param fromIndex the index of the first element (inclusive) to check.
   * @param toIndex   the index of the last element (exclusive) to check.
   *
   * @return  The greatest element found.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NoSuchElementException      if <tt>fromIndex == toIndex</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static int indexOfMax(char[] data, int fromIndex, int toIndex)
  {
    CheckArg.range(data.length, fromIndex, toIndex);
    if (fromIndex == toIndex)
      return -1;

    int index = fromIndex;  
    char max = data[fromIndex];    

    while (++fromIndex < toIndex)
    {      
      char e = data[fromIndex];
      if (e > max)
      {        
        if (e == Character.MAX_VALUE)
          return fromIndex;        
        max = e;
        index = fromIndex;
      }
    }
    return index;
  }





  /**
   * @see #indexOfMin(char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static int indexOfMin(char[] data)
  {
    return indexOfMin(data, 0, data.length);
  }


  /**
   * Returns the index of the smallest element of specified range in specified array.
   *
   * @param data      the array to search in.
   * @param fromIndex the index of the first element (inclusive) to check.
   * @param toIndex   the index of the last element (exclusive) to check.
   *
   * @return  The smallest element found.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NoSuchElementException      if <tt>fromIndex == toIndex</tt>.
   * @throws  NullPointerException        if <tt>c == null || data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static int indexOfMin(char[] data, int fromIndex, int toIndex)
  {
    CheckArg.range(data.length, fromIndex, toIndex);
    if (fromIndex == toIndex)
      return -1;

    int index = fromIndex;  
    char min = data[fromIndex];

    while (++fromIndex < toIndex)
    {
      char e = data[fromIndex];
      if (e < min)
      {        
        if (e == 0)
          return fromIndex;        
        min = e;
        index = fromIndex;
      }
    }
    return index;
  }



  /**
   * @see #max(char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static char max(char[] data)
  {
    return max(data, 0, data.length);
  }


  /**
   * Returns the geatest element of specified range in specified array.
   *
   * @param data      the array to search in.
   * @param fromIndex the index of the first element (inclusive) to check.
   * @param toIndex   the index of the last element (exclusive) to check.
   *
   * @return  The smallest element found.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NoSuchElementException      if <tt>fromIndex == toIndex</tt>.
   * @throws  NullPointerException        if <tt>c == null || data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static char max(char[] data, int fromIndex, int toIndex)
  {
    int index = indexOfMax(data, fromIndex, toIndex);
    if (index >= 0)
      return data[index];
    else
      throw new NoSuchElementException();
  }


  /**
   * @see #min(char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static char min(char[] data)
  {
    return min(data, 0, data.length);
  }


  /**
   * Returns the smallest element of specified range in specified array.
   *
   * @param data      the array to search in.
   * @param fromIndex the index of the first element (inclusive) to check.
   * @param toIndex   the index of the last element (exclusive) to check.
   *
   * @return  The smallest element found.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NoSuchElementException      if <tt>fromIndex == toIndex</tt>.
   * @throws  NullPointerException        if <tt>c == null || data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static char min(char[] data, int fromIndex, int toIndex)
  {
    int index = indexOfMin(data, fromIndex, toIndex);
    if (index >= 0)
      return data[index];
    else
      throw new NoSuchElementException();
  }







  /**
   * @see #indexOf(char[],int,int,char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static int indexOf(char[] seq, char[] data)
  {
    return indexOf(seq, 0, seq.length, data, 0, data.length);
  }

  /**
   * @see #indexOf(char[],int,int,char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static int indexOf(char[] seq, int seqFromIndex, int seqToIndex, char[] data)
  {
    return indexOf(seq, seqFromIndex, seqToIndex, data, 0, data.length);
  }

  
  
  /**
   * Searches for first occurence of specified range of specified <tt>seq</tt>uence in specified range of specified <tt>data</tt> array.
   * <br>
   * For <tt>(seqFromIndex == seqToIndex) || (fromIndex == toIndex)</tt> this method returns <tt>-1</tt>.
   *
   * @param seq           the array containing the sequence to search for.
   * @param seqFromIndex  the index (inclusive) of the first element of sequence to search for.
   * @param seqToIndex    the index (exclusive) of the last element of sequence to search for.
   * @param data          the array where to search for the sequence.
   * @param fromIndex     the index (inclusive) of first element to compare against searched sequence.  
   * @param toIndex       the index (exclusive) of last element to compare against searched sequence.
   *
   * @return Index of match if found, otherwise <tt>-1</tt>.
   *
   * @throws IndexOutOfBoundsException    for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; array.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws NullPointerException         if <tt>seq == null || data == null</tt>.
   *
   * @see #lastIndexOf(char[],int,int,char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static int indexOf(char[] seq, int seqFromIndex, int seqToIndex, char[] data, int fromIndex, int toIndex)
  {
    return indexOf(seq, seqFromIndex, seqToIndex, data, fromIndex, toIndex, false);
  }

  
  
  /**
   * Searches for first occurence of specified range of specified <tt>seq</tt>uence in specified range of specified <tt>data</tt> array.
   * <br>
   * For <tt>(seqFromIndex == seqToIndex) || (fromIndex == toIndex)</tt> this method returns <tt>-1</tt>.
   *
   * @param seq           the array containing the sequence to search for.
   * @param seqFromIndex  the index (inclusive) of the first element of sequence to search for.
   * @param seqToIndex    the index (exclusive) of the last element of sequence to search for.
   * @param data          the array where to search for the sequence.
   * @param fromIndex     the index (inclusive) of first element to compare against searched sequence.  
   * @param toIndex       the index (exclusive) of last element to compare against searched sequence.
   *
   * @return Index of match if found, otherwise <tt>-1</tt>.
   *
   * @throws IndexOutOfBoundsException    for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; array.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws NullPointerException         if <tt>seq == null || data == null</tt>.
   *
   * @see #lastIndexOf(char[],int,int,char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static int indexOf(char[] seq, int seqFromIndex, int seqToIndex, char[] data, int fromIndex, int toIndex, boolean ignoreCase)
  {
    Boolean fastShot = CheckArg.indexOfSequence(seq, seq.length, seqFromIndex, seqToIndex, data, data.length, fromIndex, toIndex);
    if (fastShot != null)
      return fastShot == Boolean.TRUE ? fromIndex : -1;
          
    // search

    final int seqLen = seqToIndex - seqFromIndex; // length of sequence
    if (seqLen == 1)
      return indexOf(data, fromIndex, toIndex, seq[seqFromIndex], ignoreCase);

              
    final int  seqHi  = seqToIndex - 1;  
    final char first  = seq[seqFromIndex]; // first element to search
    final char second = seq[seqFromIndex+1]; // second element to search
    int seqIndex      = seqFromIndex;
    char next         = first;
    int nextFirstData = -1; // index of next known occurence of first element of sequence in data
    
    for (int i = fromIndex; i < toIndex; i++)
    {
      char e = data[i];
      if (nextFirstData == -1 && Chars.equals(e, first, ignoreCase))
        nextFirstData = i;
      
      if (Chars.equals(e, next, ignoreCase))
      {
        if (seqIndex == seqHi)
          return fromIndex; // matching sequence found
        else
          next = seq[++seqIndex];        
      }
      else // mismatch
      {        
        if (nextFirstData == -1)
        {
          fromIndex = i + 1;
          next      = first;
          seqIndex  = seqFromIndex;
        }
        else
        {
          fromIndex     = i = nextFirstData;
          nextFirstData = -1;        
          seqIndex      = seqFromIndex + 1;
          next          = second;
        }
        if (toIndex - fromIndex < seqLen)
          break;
      }
    }

        
    return -1;
  }
  



  /**
   * @see #lastIndexOf(char[],int,int,char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static int lastIndexOf(char[] seq, char[] data)
  {
    return lastIndexOf(seq, 0, seq.length, data, 0, data.length);
  }

  /**
   * @see #lastIndexOf(char[],int,int,char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static int lastIndexOf(char[] seq, int seqFromIndex, int seqToIndex, char[] data)
  {
    return lastIndexOf(seq, seqFromIndex, seqToIndex, data, 0, data.length);
  }

    
  
  /**
   * Searches for last occurence of specified range of specified <tt>seq</tt>uence in specified range of specified <tt>data</tt> array.
   * <br>
   * For <tt>(seqFromIndex == seqToIndex) || (fromIndex == toIndex)</tt> this method returns <tt>-1</tt>.
   *
   * @param seq           the array containing the sequence to search for.
   * @param seqFromIndex  the index (inclusive) of the first element of sequence to search for.
   * @param seqToIndex    the index (exclusive) of the last element of sequence to search for.
   * @param data          the array where to search for the sequence.
   * @param fromIndex     the index (inclusive) of most left element to compare against searched sequence.  
   * @param toIndex       the index (exclusive) of most right element to compare against searched sequence.
   *
   * @return Index of match if found, otherwise <tt>-1</tt>.
   *
   * @throws IndexOutOfBoundsException    for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; array.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws NullPointerException         if <tt>seq == null || data == null</tt>.
   *
   * @see #indexOf(char[],int,int,char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static int lastIndexOf(char[] seq, int seqFromIndex, int seqToIndex, char[] data, int fromIndex, int toIndex)
  {
    return lastIndexOf(seq, seqFromIndex, seqToIndex, data, fromIndex, toIndex, false);
  }


  
  public static int lastIndexOf(char[] seq, int seqFromIndex, int seqToIndex, char[] data, int fromIndex, int toIndex, boolean ignoreCase)
  {
    final int seqLen = seqToIndex - seqFromIndex;
    Boolean fastShot = CheckArg.lastIndexOfSequence(seq, seq.length, seqFromIndex, seqToIndex, data, data.length, fromIndex, toIndex);
    if (fastShot != null)
      return (fastShot == Boolean.TRUE) ? (toIndex - seqLen) : -1;
      
    // search
            
    if (seqLen == 1)
      return lastIndexOf(data, fromIndex, toIndex, seq[seqFromIndex], ignoreCase);

        
    final int  seqHi  = seqToIndex - 1;  
    final char first  = seq[seqHi]; // first element to search
    final char second = seq[seqHi - 1]; // second element to search
    int seqIndex      = seqHi;
    char next         = first;
    int nextFirstData = -1; // index of next known occurence of first element of sequence in data
    
    for (int i = toIndex - 1; i >= fromIndex; i--)
    {
      char e = data[i];
      if ((nextFirstData == -1) && Chars.equals(e, first, ignoreCase))
        nextFirstData = i;
      
      if (Chars.equals(e, next, ignoreCase))
      {
        if (seqIndex == seqFromIndex)
          return toIndex - seqLen; // matching sequence found
        else
          next = seq[--seqIndex];        
      }
      else // mismatch
      {        
        if (nextFirstData == -1)
        {
          toIndex   = i;
          next      = first;
          seqIndex  = seqHi;
        }
        else
        {
          toIndex       = nextFirstData + 1;
          i             = nextFirstData;
          nextFirstData = -1;        
          seqIndex      = seqHi - 1;
          next          = second;
        }
        if (toIndex - fromIndex < seqLen)
          break;
      }
    }

        
    return -1;
  }
  




  /**
   * Creates an array of specified length filled with the specified element.
   *
   * @param e     
   *  element to fill the array with.
   * @param n
   *  length of array to be created.
   *
   * @throws NegativeArraySizeException 
   *  if {@code n < 0}.
   *
   * @since JaXLib 1.0
   */
  public static char[] nCopies(char e, int n)
  {
    char[] data = new char[n];
    
    if (e != 0)        
      fillFast(data, 0, n, e);

    return data;
  }

  
  
  



  public static int replaceEach(char[] data, char oldValue, char newValue)
  {
    return replaceEach(data, 0, data.length, oldValue, newValue);
  }

  public static int replaceEach(char[] data, int fromIndex, int toIndex, char oldValue, char newValue)
  {
    CheckArg.range(data.length, fromIndex, toIndex);
    if ((fromIndex == toIndex) || (oldValue == newValue))
      return 0;

    int count = 0;
    while (fromIndex <= --toIndex)
    {
      if (oldValue == data[toIndex])
      {
        data[toIndex] = newValue;
        count++;
      }
    }
    return count;
  }

  
  public static int replaceEach(char[] data, char oldValue, char newValue, boolean ignoreCase)
  {
    return replaceEach(data, 0, data.length, oldValue, newValue, ignoreCase);
  }

  public static int replaceEach(char[] data, int fromIndex, int toIndex, char oldValue, char newValue, boolean ignoreCase)
  {
    if (!ignoreCase)
    {
      return replaceEach(data, fromIndex, toIndex, oldValue, newValue);
    }
    else
    {
      char l = Character.toLowerCase(oldValue);
      char u = Character.toUpperCase(oldValue);
      if (l == u)
      {
        return replaceEach(data, fromIndex, toIndex, oldValue, newValue);
      }
      else
      {
        int len = data.length;
        CheckArg.range(len, fromIndex, toIndex);
        int count = 0;
        while (fromIndex < toIndex)
        {
          if (CharTools.equalsIgnoreCase(data[fromIndex], l, u))
          {
            data[fromIndex] = newValue;
            count++;
          }        
          fromIndex++;
        }
        return count;
      }
    }
  }
  
  


  /**
   * Reverses the order of the elements of specified array.
   *
   * @see #reverse(char[],int,int)
   *
   * @since JaXLib 1.0
   */
  public static void reverse(char[] a)
  {
    reverse(a, 0, a.length);
  }
  
  
  /**
   * Reverses the order of the elements of specified array in specified range.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void reverse(char[] data, int fromIndex, int toIndex)
  {
    CheckArg.rangeFromTo(fromIndex, toIndex);    
    while (fromIndex < --toIndex)
      swap(data, fromIndex++, toIndex);
  }
    
  
  /**
   * Same as <code>rotate(data, 0, data.length, distance)</code>.
   *
   * @see #rotate(char[],int,int,int)
   *
   * @since JaXLib 1.0
   */
  public static void rotate(char[] data, int distance)
  {
    rotate(data, 0, data.length, distance);
  }
  
  
  /**
   * Rotates the elements in the specified range of array by the specified distance.
   * After calling this method, the element at index <tt>i</tt> will be the element previously at index <tt>(i - distance)</tt> mod
   * <tt>toIndex-fromIndex</tt>, for all values of <tt>i</tt> between <tt>fromIndex</tt> and <tt>toIndex-1</tt>, inclusive. 
   * <p>
   * For example, suppose <tt>array</tt> comprises<tt> [t, a, n, k, s]</tt>.<br>
   * After invoking <tt>rotate(1, array, 0, array.length)</tt> (or <tt>rotate(-4, array, 0, array.length)</tt>), 
   * <tt>array</tt> will comprise <tt>[s, t, a, n, k]</tt>.
   * </p><p>
   * To move more than one element forward, increase the absolute value of the rotation distance.  
   * To move elements backward, use a positive shift distance.
   * </p>
   *
   * @param data      the array containing the elements to be rotated.
   * @param fromIndex index (inclusive) containing the first element of the range to be rotated.
   * @param toIndex   index (exclusive) containing the last element of the range to be rotated.
   * @param distance  the distance to rotate the specified range of array. There are no constraints on this value; 
   *                  it may be zero, negative, or greater than <tt>data.length</tt>.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void rotate(char[] data, int fromIndex, int toIndex, int distance) 
  {
    CheckArg.range(data.length, fromIndex, toIndex);
    int count = toIndex - fromIndex;
    if (count == 0)
      return;
    distance = distance % count;
    if (distance < 0)
      distance += count;
    if (distance == 0)
      return;
    
    for (int cycleStart = 0, nMoved = 0; nMoved != count; cycleStart++) 
    {
      char displaced = data[cycleStart + fromIndex];
      int i = cycleStart;
      do 
      {
        i += distance;
        if (i >= count)
          i -= count;
        char t = data[i + fromIndex];
        data[i + fromIndex] = displaced;
        displaced = t;
        nMoved++;
      } while (i != cycleStart);
    }
  }
  

  /**
   * @see #shuffle(char[],int,int,Random)
   *
   * @since JaXLib 1.0
   */
  public static void shuffle(char[] data)
  {
    shuffle(data, 0, data.length, null);
  }

  /**
   * @see #shuffle(char[],int,int,Random)
   *
   * @since JaXLib 1.0
   */
  public static void shuffle(char[] data, Random r)
  {
    shuffle(data, 0, data.length, r);
  }
  
  
  /**
   * Shuffles elements of specified array in specified range.
   *
   * @param r The random number generator to use for creating random indices (optional).
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void shuffle(char[] a, int fromIndex, int toIndex, Random r)
  {
    CheckArg.range(a.length, fromIndex, toIndex);
    if (r == null)
      r = new Random();
    int count = toIndex - fromIndex;
    for (int i = count; i > 1; i--)
      swap(a, (fromIndex + i) - 1, fromIndex + r.nextInt(i));
  }
  
  

  /**
   * Sorts specified array using {@link SortAlgorithm#getDefault() default} sort algorithm.
   *
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void sort(char[] data)
  {
    SortAlgorithm.getDefault().apply(data);
  }
  
  /**
   * Sorts specified range of specified array using {@link SortAlgorithm#getDefault() default} sort algorithm.
   *
   * @throws  IndexOutOfBoundsException   for an illegal endpoint index value <tt>(fromIndex &lt; 0 || toIndex &gt; data.length 
   *                                      || fromIndex &gt; toIndex)</tt>.
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void sort(char[] data, int fromIndex, int toIndex)
  {
    SortAlgorithm.getDefault().apply(data, fromIndex, toIndex);
  }
  
  
  
  /**
   * Replaces element of specified array at <tt>index1</tt> with element at <tt>index2</tt>, and vice-versa.
   *
   * @throws  IndexOutOfBoundsException   for an illegal index value <tt>(index &lt; 0 || index &ge; data.length)</tt>. 
   * @throws  NullPointerException        if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void swap(char[] data, int index1, int index2)
  {
    char t = data[index1];
    data[index1] = data[index2];
    data[index2] = t;
  }  
  
  
  /**
   * Replaces element of specified first array at <tt>index1</tt> with element of specified second array at <tt>index2</tt>, and vice-versa.
   *
   * @throws  IndexOutOfBoundsException   for an illegal index value <tt>(index &lt; 0 || index &ge; array.length)</tt>. 
   * @throws  NullPointerException        if <tt>(a == null) || (b == null)</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void swap(char[] a, int index1, char[] b, int index2)
  {
    char t = a[index1];
    a[index1] = b[index2];
    b[index2] = t;
  }  

  
  /**
   * Replaces element of specified first array at <tt>index1</tt> with element of specified stringbuffer at <tt>index2</tt>, and vice-versa.
   *
   * @throws  IndexOutOfBoundsException   for an illegal index value <tt>(index &lt; 0 || index &ge; array.length)</tt>. 
   * @throws  NullPointerException        if <tt>(a == null) || (b == null)</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void swap(char[] a, int index1, StringBuffer b, int index2)
  {
    char t = a[index1];
    a[index1] = b.charAt(index2);
    b.setCharAt(index2, t);
  }  
  
  
  /**
   * Swaps data[index1 .. (index1 + count-1)] with data[index2 .. (index2 + count-1)].
   *
   * @throws IllegalArgumentException   if <tt>count < 0</tt>
   * @throws IndexOutOfBoundsException  for an illegal index value <tt>(index &lt; 0 || index &ge; data.length)</tt>. 
   * @throws IndexOutOfBoundsException  if <tt>(index1 + count > data.length) || (index2 + count > data.length)</tt>.
   * @throws NullPointerException       if <tt>data == null</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void swapRanges(char[] data, int index1, int index2, int count)
  {
    CheckArg.swapRanges(count, data.length, index1, index2);
    if (index1 != index2)
    {
      while (count-- > 0)
        swap(data, index1++, index2++);
    }
  }

  
  /**
   * Swaps <code>a[0 .. length]</code> with <code>b[0 .. length]</code>.
   *
   * @throws IllegalArgumentException if <code>a.length != b.length</code>.
   * @throws NullPointerException     if <code>(a == null) || (b == null)</code>.
   */
  public static void swap(char[] a, char[] b)
  {
    if (a != b)
    {
      swapRanges(a, 0, b, 0, Math.max(a.length, b.length));
    }
  }
  


  /**
   * Swaps a[index1 .. (index1 + count-1)] with b[index2 .. (index2 + count-1)].
   *
   * @throws IllegalArgumentException   if <tt>count < 0</tt>
   * @throws IndexOutOfBoundsException  for an illegal index value <tt>(index &lt; 0 || index &ge; array.length)</tt>. 
   * @throws IndexOutOfBoundsException  if <tt>(index1 + count &gt; a.length) || (index2 + count &gt; b.length)</tt>.
   * @throws NullPointerException       if <tt>(a == null) || (b == null)</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void swapRanges(char[] a, int index1, char[] b, int index2, int count)
  {
    if (a == b)
    {
      swapRanges(a, index1, index2, count);
      return;
    }    
    CheckArg.swapRanges(a.length, index1, b.length, index2, count);
    
    while (count-- > 0)      
      swap(a, index1++, b, index2++);
  }

  
  /**
   * Swaps a[index1 .. (index1 + count-1)] with b[index2 .. (index2 + count-1)].
   *
   * @throws IllegalArgumentException   if <tt>count < 0</tt>
   * @throws IndexOutOfBoundsException  for an illegal index value <tt>(index &lt; 0 || index &ge; array.length)</tt>. 
   * @throws IndexOutOfBoundsException  if <tt>(index1 + count &gt; a.length) || (index2 + count &gt; b.length)</tt>.
   * @throws NullPointerException       if <tt>(a == null) || (b == null)</tt>.
   *
   * @since JaXLib 1.0
   */
  public static void swapRanges(char[] a, int index1, StringBuffer b, int index2, int count)
  {
    CheckArg.swapRanges(count, a.length, index1, b.length(), index2);
    
    while (count-- > 0)      
      swap(a, index1++, b, index2++);
  }
  


  




}
