/*
 * Copyright 2005 Frank W. Zammetti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package javawebparts.taglib.basicstr;


import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javawebparts.core.org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class is a custom tag that returns a portion of a string.  This
 * function can work in a number of ways, defined by setting the following
 * attributes:
 * <br><br>
 * type - This is the type of substring to get.  Valid values are "mid",
 * "left" and "right".  Required: Yes.
 * <br>
 * count - This attribute is required when type is "left" or "right".  It is
 * the number of characters from the left or right of the source string,
 * depending on what type is, that will be returned.  If this value is <= 0
 * then nothing will returned.  If this value is > the length of the source
 * string then the entire source string will be returned.
 * Required: Yes, if type is "left" or "right".
 * <br>
 * start - This attribute is required when type is "mid".  It is the 1-based
 * character the substring begins at, inclusive.  In other words, the first
 * character of the string is 1, so a value of 1 will start with the first
 * character.  If this value is less than 1 or is greater than the length of
 * the source string, the value 1 will be used.  Required: Yes, if type
 * is "mid".
 * <br>
 * end - This attribute is required when type is "mid".  It is the 1-based
 * character the substring ends at, inclusive.  If this value is less than 1 or
 * is greater than the length of the source string, the length of the string
 * will be user.  Required: Yes, if type is "mid".
 * <br><br>
 * Note that in all cases if an attribute is unparsable as a number either
 * because it is not present or has a non-parsable value, this tag will
 * return nothing.
 *
 * @author <a href="mailto:fzammetti@omnytex.com">Frank W. Zammetti</a>.
 */
public class SubstrTag extends BodyTagSupport {


  /**
   * This static initializer block tries to load all the classes this one
   * depends on (those not from standard Java anyway) and prints an error
   * meesage if any cannot be loaded for any reason.
   */
  static {
    try {
      Class.forName("javax.servlet.jsp.JspException");
      Class.forName("javax.servlet.jsp.JspWriter");
      Class.forName("javax.servlet.jsp.PageContext");
      Class.forName("javax.servlet.jsp.tagext.BodyTagSupport");
      Class.forName("javawebparts.core.org.apache.commons.lang.StringUtils");
      Class.forName("org.apache.commons.logging.Log");
      Class.forName("org.apache.commons.logging.LogFactory");
    } catch (ClassNotFoundException e) {
      System.err.println("SubstrTag" +
        " could not be loaded by classloader because classes it depends" +
        " on could not be found in the classpath...");
      e.printStackTrace();
    }
  }


  /**
   * Log instance.
   */
  private static Log log = LogFactory.getLog(SubstrTag.class);


  /**
   * This is the body content text to be altered.
   */
  private String text = "";


  /**
   * This is the trim type attribute.  Valid values are left, right, both.
   */
  private String type = "";


  /**
   * Number of characters to return when doing "left" or "right" type.
   */
  private String count;


  /**
   * Start of substring when type is "mid".
   */
  private String start;


  /**
   * End of substring when type is "mid".
   */
  private String end;


  /**
   * type mutator.
   *
   * @param inType Trim type.
   */
  public void setType(String inType) {

    type = inType;

  } // End setType().


  /**
   * count mutator.
   *
   * @param inCount count.
   */
  public void setCount(String inCount) {

    count = inCount;

  } // End setCount().


  /**
   * start mutator.
   *
   * @param inStart start.
   */
  public void setStart(String inStart) {

    start = inStart;

  } // End setStart().


  /**
   * end mutator.
   *
   * @param inEnd end.
   */
  public void setEnd(String inEnd) {

    end = inEnd;

  } // End setEnd().


  /**
   * Alter the body content.
   *
   * @return              Return code.
   * @throws JspException If anything goes wrong.
   */
  public int doAfterBody() throws JspException {

    String bcs = bodyContent.getString();
    if (bcs == null) {
      bcs = "";
    }
    if (type.equalsIgnoreCase("left")) {
      try {
        int iCount = Integer.parseInt(count);
        if (iCount < 0) {
          iCount = 0;
        }
        text = StringUtils.left(bcs, iCount);
      } catch (NumberFormatException nfe) {
        text = bcs;
      }
    }
    if (type.equalsIgnoreCase("right")) {
      try {
        int iCount = Integer.parseInt(count);
        if (iCount < 0) {
          iCount = 0;
        }
        text = StringUtils.right(bcs, iCount);
      } catch (NumberFormatException nfe) {
        text = bcs;
      }
    }
    if (type.equalsIgnoreCase("mid")) {
      try {
        int iStart = Integer.parseInt(start);
        if (iStart < 0 || iStart > bcs.length()) {
          iStart = 0;
        }
        int iEnd = Integer.parseInt(end);
        if (iEnd < 0 || iEnd > bcs.length()) {
          iEnd = bcs.length();
        }
        if (iStart > 0) {
          iStart = iStart -1;
        }
        text = bcs.substring(iStart, iEnd);
      } catch (NumberFormatException nfe) {
        text = bcs;
      }
    }
    return SKIP_BODY;

  } // End doAfterBody().


  /**
   * Render the altered body.
   *
   * @return              Return code.
   * @throws JspException If anything goes wrong.
   */
  public int doEndTag() throws JspException {

    try {
      JspWriter out = pageContext.getOut();
      out.print(text);
    } catch (IOException ioe) {
      throw new JspException(ioe.toString());
    }
    return EVAL_PAGE;

  } // doEndTag().


} // End class.
