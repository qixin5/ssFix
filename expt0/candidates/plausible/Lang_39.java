/**
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM - initial API and implementation
 *
 * $Id: ProductUpdateBuilder.java,v 1.4 2006/03/02 23:13:29 nickb Exp $
 */
package org.eclipse.releng.generators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.osgi.framework.BundleException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.eclipse.osgi.framework.util.Headers;


/**
 * Utility class to convert buildmanifest.properties files into fetch.xml files.
 */
public class ProductUpdateBuilder extends org.eclipse.releng.generators.AbstractApplication
{

  protected boolean add4thPart;

  protected int debug = -1;

  protected String rootLocation;

  protected String siteLocation;

  protected String siteXML;

  protected String buildNumber;

  protected String buildDesc;

  protected Map plugins;

  protected List features;

  protected Properties properties;

  private String propertiesFile;

  class FeatureData
  {
    FeatureData(String id, String version, String os, String ws, String arch, String jar)
    {
      this.jar = jar;
      this.id = id;
      this.version = version;
      this.os = os;
      this.ws = ws;
      this.arch = arch;
    }

    String jar;

    String id;

    String version;

    String os;

    String ws;

    String arch;

  }

  public ProductUpdateBuilder() throws Exception
  {
    properties = new Properties();
    plugins = new HashMap();
    features = new ArrayList();
  }

  public static void main(String[] args) throws Exception
  {
    ProductUpdateBuilder modifier = new ProductUpdateBuilder();
    modifier.run(args);
  }

  public void run()
  {
    try
    {
      loadProperties();
      modifyPlugins();
      modifyFeatures();
      writeSiteXML();
      System.out.println("Done");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void loadProperties() throws FileNotFoundException, IOException
  {
    if (propertiesFile!=null) {
      properties.load(new FileInputStream(propertiesFile));
    } else {
      System.out.println("Cannot load properties: please specify -propertiesFile ./path/to/file.properties on commandline");

    }
  }

  protected void modifyFeatures() throws Exception
  {
    File root = new File(rootLocation, "features");
    String[] features = root.list();
    if (features == null)
      System.out.println("Could not find features in " + rootLocation + ".");
    for (int i = 0; i < features.length; i++)
    {
      File elementRoot = new File(root, features[i]);
      File element = new File(elementRoot, "feature.xml");
      if (!element.exists())
      {
        System.out.println("Could not find descriptor for: " + features[i]);
      }
      else
      {
        modifyFeature(element);
      }
    }
  }

  protected void modifyPlugins() throws Exception
  {
    File root = new File(rootLocation, "plugins");
    String[] plugins = root.list();

    if (plugins == null)
      System.out.println("Could not find plugins in " + rootLocation + ".");
    for (int i = 0; i < plugins.length; i++)
    {
      File elementRoot = new File(root, plugins[i]);
      File bundleElement = new File(elementRoot, File.separator + "META-INF" + File.separator + "MANIFEST.MF");
      File pluginElement = new File(elementRoot, "plugin.xml");
      boolean found = (bundleElement.exists() && pluginElement.exists());
      File element = new File(elementRoot, "plugin.xml");
      if (element.exists())
      {
        modifyPlugin(element, "plugin", found);
        found = true;
      }
      element = new File(elementRoot, File.separator + "META-INF" + File.separator + "MANIFEST.MF");
      if (element.exists())
      {
        modifyPlugin(element, "bundle", found);
        found = true;
      }
      element = new File(elementRoot, "fragment.xml");
      if (element.exists())
      {
        modifyPlugin(element, "fragment", found);
        found = true;
      }

      if (!found)
        System.out.println("Could not find descriptor for: " + plugins[i]);
    }

  }

  protected String getTag(String value)
  {
    String[] values = getArrayFromString(value);
    if (values == null || values.length == 0)
      return "none";
    else
      return values[0];
  }

  /**
   * Convert a list of tokens into an array. The list separator has to be
   * specified.
   */
  public static String[] getArrayFromString(String list, String separator)
  {
    if (list == null || list.trim().equals(""))
      return new String [0];
    ArrayList result = new ArrayList();
    for (StringTokenizer tokens = new StringTokenizer(list, separator); tokens.hasMoreTokens();)
    {
      String token = tokens.nextToken().trim();
      if (!token.equals(""))
        result.add(token);
    }
    return (String[])result.toArray(new String [result.size()]);
  }

  /**
   * convert a list of comma-separated tokens into an array
   */
  public static String[] getArrayFromString(String list)
  {
    return getArrayFromString(list, ",");
  }

  protected void modifyFeature(File descriptor) throws Exception
  {
    String name = descriptor.getParentFile().getName();
    String id = name.substring(0, name.indexOf("_"));
    fixFeatureXml(descriptor, id, verifyQualifier(buildNumber));
  }

  protected File renameFolder(File oldName, String id, String version)
  {
    if (version.indexOf(".qualifier")>=0) return oldName; // do not rename if ".qualifier"
    File newName = new File(oldName.getParent(), id + "_" + version);
    if (oldName.getAbsolutePath().equals(newName.getAbsolutePath())) return oldName; // no change, nothing to do
    String result = (oldName.renameTo(newName) ? "Success" : "Failed");
    if (debug > 0) System.out.println(result + " on renaming " + oldName.getAbsolutePath() + " to " + newName.getAbsolutePath());
    return newName;
  }

  private void fixFeatureXml(File descriptor, String featureId, String featureTag) throws Exception
  {

    // load feature.xml
    StringBuffer xml = readFile(descriptor);

    // store feature config information
    String os = getAttributeValue("feature", "os", descriptor);
    String ws = getAttributeValue("feature", "ws", descriptor);
    String arch = getAttributeValue("feature", "arch", descriptor);

    // fix up feature version
    int start = scan(xml, 0, "<feature");
    start = scan(xml, start, "version");

    /*
     * HACK verify that this is the version attribute by checking for an
     * equals sign within the next few spaces. Not sure why xml parser not
     * being used here.
     */
    int equalsIndex = scan(xml, start, "=");
    if ((equalsIndex - (start + 7)) > 3)
      start = scan(xml, equalsIndex, "version");

    start = scan(xml, start, "\"");
    int end = scan(xml, start + 1, "\"");
    String version = xml.substring(start + 1, end);
    String[] va;
    if (this.add4thPart)
    {
      va = versionToArray(version);
      if (va[3].equals("")) va[3] = featureTag;
      if (va[3].equals("qualifier")) va[3] = "v" + buildNumber.substring(1); // hack
      version = arrayToVersion(va);
    }

    xml = xml.replace(start + 1, end, version);
    String featureVersion = version;

    if (debug > 1) {
      System.out.println(" * start: "+start);
      System.out.println(" * version: "+version);
      System.out.println(" * featureVersion: "+featureVersion);
    }

    if (featureVersion.indexOf("qualifier")>=0) {
      featureVersion = featureVersion.substring(0, featureVersion.indexOf(".qualifier")) + ".v" + buildNumber.substring(1); 
      if (debug > 1) System.out.println(" * featureVersion: "+featureVersion + " (revised)");
    }

    int startForSubTags = start;

    // go through the includes elements and fix up feature versions
    int includesStart = scan(xml, start + 1, "<includes");
    while (includesStart != -1)
    {
      start = scan(xml, includesStart, "version");

      /*
       * HACK verify that this is the version attribute by checking for an
       * equals sign within the next few spaces. Not sure why xml parser
       * not being used here.
       */
      equalsIndex = scan(xml, start, "=");
      if ((equalsIndex - (start + 7)) > 3)
        start = scan(xml, equalsIndex, "version");

      start = scan(xml, start, "\"");
      end = scan(xml, start + 1, "\"");
      version = xml.substring(start + 1, end);

      if (this.add4thPart)
      {
        va = versionToArray(version);
        if (va[3].equals("")) va[3] = featureTag;
        if (va[3].equals("qualifier")) va[3] = "v" + buildNumber.substring(1); // hack
        version = arrayToVersion(va);
      }

      xml = xml.replace(start + 1, end, version);
      includesStart = scan(xml, start + 1, "<includes");
    }

    if (debug > 1) {
       System.out.println(" * start: "+start);
       System.out.println(" * version: "+version);
       System.out.println(" * includesStart: "+includesStart);
    }

    start = startForSubTags;

    // go through the plugin elements and fix up their version
    int pluginStart = scan(xml, start + 1, "<plugin");
    while (pluginStart != -1)
    {
      start = scan(xml, pluginStart, "id");
      start = scan(xml, start, "\"");
      end = scan(xml, start + 1, "\"");
      String id = xml.substring(start + 1, end);
      String tag = (String)plugins.get(id);
      if (tag != null)
      {
        start = scan(xml, end, "version");

        /*
         * HACK verify that this is the version attribute by checking
         * for an equals sign within the next few spaces. Not sure why
         * xml parser not being used here.
         */
        equalsIndex = scan(xml, start, "=");
        if ((equalsIndex - (start + 7)) > 3)
          start = scan(xml, equalsIndex, "version");

        start = scan(xml, start, "\"");
        end = scan(xml, start + 1, "\"");
        version = xml.substring(start + 1, end);

        if (this.add4thPart)
        {
          va = versionToArray(version);
          if (va[3].equals("")) va[3] = tag;
          if (va[3].equals("qualifier")) va[3] = "v" + buildNumber.substring(1); // hack
          version = arrayToVersion(va);
        }
        xml = xml.replace(start + 1, end, version);
      }
      pluginStart = scan(xml, pluginStart + 7, "<plugin");
    }

    if (debug > 1) {
      System.out.println(" * start: "+start);
      System.out.println(" * version: "+version);
      System.out.println(" * pluginStart: "+pluginStart);
    }

    // write it out to the descriptor location
    char[] outbuf = xml.toString().toCharArray();
    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(descriptor));
    try
    {
      writer.write(outbuf);
    }
    finally
    {
      try
      {
        writer.close();
      }
      catch (IOException e)
      {
      }
    }
    File newFolder = renameFolder(descriptor.getParentFile(), featureId, featureVersion);
    writeJAR(newFolder, "features");

    features.add(new FeatureData(featureId, featureVersion, os, ws, arch, newFolder.getName() + ".jar"));
  }

  private StringBuffer readFile(File target) throws IOException
  {
    FileInputStream fis = new FileInputStream(target);
    InputStreamReader reader = new InputStreamReader(fis);
    StringBuffer result = new StringBuffer();
    char[] buf = new char [4096];
    int count;
    try
    {
      count = reader.read(buf, 0, buf.length);
      while (count != -1)
      {
        result.append(buf, 0, count);
        count = reader.read(buf, 0, buf.length);
      }
    }
    finally
    {
      try
      {
        fis.close();
        reader.close();
      }
      catch (IOException e)
      {
      }
    }
    return result;
  }

  protected void modifyPlugin(File descriptor, String type, boolean pluginandbundle) throws Exception
  {
    String name = descriptor.getParentFile().getName();
    if (type.equals("bundle"))
    {
      File parentDir = descriptor.getParentFile();
      name = parentDir.getParentFile().getName();
    }
    String id = name.substring(0, name.indexOf("_"));
    String tag = verifyQualifier(buildNumber); // use submitted value not value from mapfile for consistency!
    if (type.equals("bundle"))
    {
      fixBundle(descriptor, id, tag, type, pluginandbundle);
    }
    else
    {
      fixPluginXml(descriptor, id, tag, type, pluginandbundle);
    }

    plugins.put(id, tag);
  }

  private void fixPluginXml(File descriptor, String id, String qualifier, String type, boolean pluginandbundle) throws Exception
  {

    // load plugin.xml or fragment.xml
    StringBuffer xml = readFile(descriptor);

    // fix up version
    int start = scan(xml, 0, "<" + type);
    if (start > -1)
    {
      start = scan(xml, start, "version");

      if (start > -1)
      {
        /*
         * HACK verify that this is the version attribute by checking
         * for an equals sign within the next few spaces. Not sure why
         * xml parser not being used here.
         */
        int equalsIndex = scan(xml, start, "=");
        if ((equalsIndex - (start + 7)) > 3)
          start = scan(xml, equalsIndex, "version");
        if (start > -1)
        {
          start = scan(xml, start, "\"");
          if (start > -1)
          {
            int end = scan(xml, start + 1, "\"");
            if (end > -1)
            {

              /*
               * HACK If the plugin.xml doesn't have a version
               * string, refer to the Manifest
               */

              String version = xml.substring(start + 1, end);

              if (this.add4thPart)
              {
                String[] va = versionToArray(version);
                if (va[3].equals("")) va[3] = qualifier;
                if (va[3].equals("qualifier")) va[3] = "v" + buildNumber.substring(1); // hack // hack
                version = arrayToVersion(va);
              }

              xml = xml.replace(start + 1, end, version);

              // write it out to the descriptor location
              char[] outbuf = xml.toString().toCharArray();
              OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(descriptor));
              try
              {
                writer.write(outbuf);
              }
              finally
              {
                try
                {
                  writer.close();
                }
                catch (IOException e)
                {
                }
              }
              if (debug > 0) System.out.println("Updated " + type + " for " + descriptor.getAbsolutePath());
              if (!(pluginandbundle))
              {
                File newFolder = renameFolder(descriptor.getParentFile(), id, version);
                writeJAR(newFolder, "plugins");
              }
            }
          }
        }
      }
    }
  }

  private void fixBundle(File descriptor, String id, String qualifier, String type, boolean pluginandbundle) throws Exception
  {

    // load plugin.xml or fragment.xml or MANIFEST.MF
    StringBuffer xml = readFile(descriptor);

    Headers headers = null;
    try
    {
      headers = Headers.parseManifest(new FileInputStream(new File(descriptor, "")));
    }
    catch (BundleException e)
    {
      e.printStackTrace();
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }

    // String version =
    // descriptor.getName()+";bundle-version=\"["+headers.get("Bundle-version").toString();
    String version = headers.get("Bundle-version").toString();
    String vtitle = "Bundle-Version: " + version;

    // fix up version
    int start = scan(xml, 0, vtitle);

    if (this.add4thPart)
    {
      String[] va = versionToArray(version);
      if (va[3].equals("")) va[3] = qualifier;
      if (va[3].equals("qualifier")) va[3] = "v" + buildNumber.substring(1); // hack
      version = arrayToVersion(va);
    }

    int end = start + vtitle.length();
    xml = xml.replace(start, end, "Bundle-Version: " + version);

    // write it out to the descriptor location
    descriptor = descriptor.getAbsoluteFile();

    char[] outbuf = xml.toString().toCharArray();
    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(descriptor));
    try
    {
      writer.write(outbuf);
    }
    finally
    {
      try
      {
        writer.close();
      }
      catch (IOException e)
      {
      }
    }
    if (debug > 0)
    {
      System.out.println("Updated " + type + " for " + descriptor.getAbsolutePath());
    }
    File n = descriptor.getParentFile();
    n = n.getParentFile();
    File newFolder = renameFolder(n, id, version);
    writeJAR(newFolder, "plugins");

  }

  private void writeJAR(File pluginFolder, String rootFolder) throws Exception
  {
    String[] list = pluginFolder.list();
    if (list == null)
    {
      System.out.println("No files found in: " + pluginFolder.getAbsolutePath());
      return;
    }
    File destination = new File(siteLocation, rootFolder);
    destination.mkdirs();
    String jarName = pluginFolder.getName() + ".jar";
    JarOutputStream jos = null;

    try
    {
      File jarFile = new File(destination, jarName);
      if (debug > 0)
      {
        System.out.println("Writing " + jarFile.getAbsolutePath());
      }
      jos = new JarOutputStream(new FileOutputStream(jarFile));
      writeJAREntries(jos, pluginFolder, 0);
    }
    finally
    {
      if (jos != null)
        try
      {
          jos.close();
      }
      catch (IOException e)
      {
      }
    }
  }

  protected void writeSiteXML() throws Exception
  {
    StringWriter extraInfo = new StringWriter();
    PrintWriter writer = new PrintWriter(extraInfo);

    StringWriter categoryExtraInfo = new StringWriter();
    PrintWriter categoryWriter = new PrintWriter(categoryExtraInfo);

    try
    {
      for (Iterator iter = features.iterator(); iter.hasNext();)
      {
        FeatureData data = (FeatureData)iter.next();
        writer.println();
        writer.print("\t");
        writer.print("<feature url=\"features/");
        writer.print(data.jar);
        writer.print("\" patch=\"");
        writer.print("false");
        writer.print("\" id=\"");
        writer.print(data.id);
        writer.print("\" version=\"");
        writer.print(data.version);
        // add os, ws and arch attributes if they exist
        if (data.os != null)
        {
          writer.print("\" os=\"" + data.os);
        }
        if (data.ws != null)
        {
          writer.print("\" ws=\"" + data.ws);
        }
        if (data.arch != null)
        {
          writer.print("\" arch=\"" + data.arch);
        }
        writer.println("\">");

        // lump it into the SDK bundle/group
        writer.println("\t\t<category name=\"" + properties.getProperty("_SDK_Name") + " "
          + buildDesc + (add4thPart ? " " + buildNumber : "") + "\"/>"); // new 040910
        writer.println("\t</feature>");
        writer.println();
      }

      categoryWriter.println();
      categoryWriter.println("\t<category-def label=\""
        + properties.getProperty("_SDK_Name") + " "
        + buildDesc + (add4thPart ? " " + buildNumber : "")
        + (properties.getProperty("_SDK_Subprojects") != null && !properties.getProperty("_SDK_Subprojects").equals("") ?
          " " + properties.getProperty("_SDK_Subprojects") : "")
          + "\" name=\"" + properties.getProperty("_SDK_Name") + " "
          + buildDesc + (add4thPart ? " " + buildNumber : "") + "\">");
      categoryWriter.println("\t\t<description>");
      categoryWriter.println("This category contains the various features of the " + buildDesc + (add4thPart ? " " + buildNumber : "")
        + " build of " + properties.getProperty("_SDK_Name") + ", which includes "
        + properties.getProperty("_Included_Files") + ".");
      categoryWriter.println("\t\t</description>");
      categoryWriter.println("\t</category-def>");
    }
    finally
    {
      if (writer != null)
      {
        writer.flush();
        writer.close();
      }
    }

    // read site.xml
    File originalXML = new File(siteLocation + "/" + siteXML);
    StringBuffer site = readFile(originalXML);
    int pos = scan(site, 0, "<category-def");
    if (pos == -1)
    {
      pos = scan(site, 0, "</site");
    }
    if (pos == -1)
    {
      System.out.println("Error writing " + siteLocation + "/" + siteXML + ".  Initial file and new file are both probably invalid");
      pos = 0;
    }

    // Insert features either before the first <category-def or, if there
    // are
    // no category-def, then before the /site.
    site.insert(pos, extraInfo.toString());

    // Insert category-def before the /site
    pos = scan(site, 0, "</site");
    if (pos == -1)
    {
      System.out.println("Error writing " + siteLocation + "/" + siteXML + ".  Initial file and new file are both probably invalid");
    }
    else
    {
      site.insert(pos, categoryExtraInfo.toString());
    }
    File destination = new File(siteLocation + "/" + siteXML);
    FileOutputStream fos = new FileOutputStream(destination);
    try
    {
      PrintWriter pw = new PrintWriter(fos);
      pw.write(site.toString());
      pw.flush();
      pw.close();
    }
    finally
    {
      fos.close();
    }
  }

  protected void writeJAREntries(JarOutputStream jos, File folder, int rootMembers) throws Exception
  {
    File[] list = folder.listFiles();
    if (list == null)
    {
      System.out.println("No files found in: " + folder.getAbsolutePath());
      return;
    }
    String prefix = "";
    File temp = folder;
    for (int i = 0; i < rootMembers; i++)
    {
      prefix = temp.getName() + "/" + prefix;
      temp = temp.getParentFile();
    }
    rootMembers++;
    for (int i = 0; i < list.length; i++)
    {
      if (list[i].isDirectory())
      {
        writeJAREntries(jos, list[i], rootMembers);
        continue;
      }
      FileInputStream is = new FileInputStream(list[i]);
      try
      {
        writeJarEntry(prefix + list[i].getName(), is, jos);
      }
      finally
      {
        if (is != null)
          try
        {
            is.close();
        }
        catch (IOException e)
        {
        }
        is = null;
      }
    }
  }

  private void writeJarEntry(String entryName, InputStream is, JarOutputStream jos) throws Exception
  {

    ZipEntry jarEntry = new ZipEntry(entryName);
    byte[] buf = new byte [4096];
    int count;
    jos.putNextEntry(jarEntry);
    count = is.read(buf);
    while (count != -1)
    {
      jos.write(buf, 0, count);
      count = is.read(buf);
    }
  }

  private String arrayToVersion(String[] a)
  {
    return a[0] + "." + a[1] + "." + a[2] + "." + a[3];
  }

  private String[] versionToArray(String s)
  {
    String[] a = new String []{ "0", "0", "0", "" };
    StringTokenizer t = new StringTokenizer(s, ".");
    for (int i = 0; i < 4 && t.hasMoreTokens(); i++)
    {
      a[i] = t.nextToken();
    }
    return a;
  }

  private int scan(StringBuffer buf, int start, String target)
  {
    return scan(buf, start, new String []{ target });
  }

  private int scan(StringBuffer buf, int start, String[] targets)
  {
    for (int i = start; i < buf.length(); i++)
    {
      for (int j = 0; j < targets.length; j++)
      {
        if (i < buf.length() - targets[j].length())
        {
          String match = buf.substring(i, i + targets[j].length());
          if (targets[j].equals(match))
            return i;
        }
      }
    }
    // System.out.println("*** buf: "+buf.toString());
    return -1;
  }

  protected void processCommandLine(List commands)
  {
    // looks for param/arg-like commands

    // Path to unzipped eclipse directory example - c:\eclipse
    String[] arguments = getArguments(commands, "-root");
    this.rootLocation = arguments[0]; // only consider one location

    // String used to name the build. Only appears in the Category-def name
    arguments = getArguments(commands, "-build");
    this.buildNumber = arguments[0]; // only consider one location

    // String used to name the build. Only appears in the Category-def name
    arguments = getArguments(commands, "-buildDesc");
    this.buildDesc = arguments[0]; // only consider one location

    // Path to the sites root directory. IE the directory that will contain
    // the
    // plugins and featues directories for the geneated update site.
    arguments = getArguments(commands, "-site");
    this.siteLocation = arguments[0]; // only consider one location

    // Full path and name of the site.xml file.
    arguments = getArguments(commands, "-sitexml");
    this.siteXML = arguments[0];

    // Include if you want to generate 4 part version numbers.
    // Leave off if you want to generate 3 part persion numbers.
    this.add4thPart = commands.contains("-add4thPart");

    // name of the properties file to use for strings
    arguments = getArguments(commands, "-propertiesFile");
    this.propertiesFile = arguments[0];

    // extra debugging comments?
    arguments = getArguments(commands, "-debug");
    if (arguments[0] != null && !arguments[0].equals(""))
    {
      this.debug = Integer.parseInt("0" + arguments[0]);
    }
  }

  /*
   * may have to come back and add a case here for if we want to label 2.0.0.1
   * as a maintenance build 2.0.0.M1-200407321234M however, once we move to a
   * "M" prefix, assume either all subsequent drops are all "M" prefixed:
   * 2.0.0.2 -> 2.0.0.M2-200408321234M, 2.0.0.3 -> 2.0.0.M3-200408321234M,
   * etc. then, with 2.0.1, we can revert to 2.0.1 -> 2.0.1.200409311234I,
   * etc.
   */

  private String verifyQualifier(String s)
  {
    if (s.indexOf("build_") == 0)
    { // starts with "build_"
      String t = s.substring(6);
      if (t.indexOf("I") == 0 || t.indexOf("M") == 0 || t.indexOf("R") == 0 || t.indexOf("S") == 0 || t.indexOf("N") == 0)
      {
        t = t.substring(1) + t.substring(0, 1); // move letter to the
        // end
      }
      if (debug > 1)
      {
        System.out.println("[UBEj::vQ()] Qualifier converted from [" + s + "] to [" + t + "]");
      }
      s = t;
    }
    char[] chars = s.trim().toCharArray();
    boolean whitespace = false;
    for (int i = 0; i < chars.length; i++)
    {
      if (!Character.isLetterOrDigit(chars[i]))
      {
        chars[i] = '-';
        whitespace = true;
      }
    }
    return whitespace ? new String(chars) : s;
  }

  private String getAttributeValue(String elementName, String attributeName, File file)
  {

    class Handler extends DefaultHandler
    {

      String element;

      String attribute;

      String value;

      Handler(String element, String attribute)
      {
        this.element = element;
        this.attribute = attribute;
      }

      // Start Element Event Handler
      public void startElement(String uri, String local, String qName, Attributes atts)
      {

        String result = atts.getValue(attribute);

        if (qName.equals(element))
        {
          value = result;
        }
      }
    }

    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    SAXParser parser;

    try
    {
      Handler handler = new Handler(elementName, attributeName);
      parser = saxParserFactory.newSAXParser();
      parser.parse(file, handler);
      return handler.value;

    }
    catch (ParserConfigurationException e)
    {
      e.printStackTrace();
    }
    catch (SAXException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }

}