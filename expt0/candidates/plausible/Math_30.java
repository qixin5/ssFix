/* 
 * Copyright (c) 2001 by Matt Welsh and The Regents of the University of 
 * California. All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice and the following
 * two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Author: Matt Welsh <mdw@cs.berkeley.edu>
 * 
 */

import seda.util.MDWUtil;
import seda.util.StatsGatherer;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

/**
 * This is an HTTP load generator which operates similarly to that used
 * in the SPECweb99 benchmark. Unlike SPECweb99, it only accesses static
 * pages and measures throughput and request/connect time latency (rather
 * than the number of simultaneous "valid" connections).
 *
 * @author Matt Welsh
 */

public class HttpLoadThreaded extends Thread {
  
  private static final boolean DEBUG = false;

  private static final boolean REPORT = true;
  private static final boolean HISTOGRAM_REPORT = true;

  // Time in ms between reporting measurements
  private static final int BENCH_DELAY = 5000;

  // Number of samples to skip at beginning of run
  public static final int SKIP_SAMPLES = 0;

  // Wait this long before actually doing data send (first connection only)
  //private static final int CONNECT_DELAY = 5000;
  private static final int CONNECT_DELAY = 0;

  // Wait this long for each client thread when exiting
  private static final int MAX_JOIN_TIME = 1000;

  // Number of requests before closing connection; -1 for infinite
  public static final int MAX_REQS_PER_CONN = 5;
  //public static final int MAX_REQS_PER_CONN = -1;

  // Number of bench samples before we exit; if zero, run forever
  private static int NUMBER_RUNS = 100;

  // Bucket size for connection time histogram
  private static final int CONN_HIST_BUCKETSIZE = 1;

  // Bucket size for response time histogram
  private static final int RESP_HIST_BUCKETSIZE = 1;

  // Number of classes
  private static final int NUMCLASSES = 4;
  // Number of directories - based on load value
  private static int NUMDIRS;
  // Number of files
  private static final int NUMFILES = 8;
  // Zipf distribution table for directory
  private static double DIR_ZIPF[];
  // Zipf distribution table for file
  private static double FILE_ZIPF[];
  // Frequency of each class
  private static final double CLASS_FREQ[] = { 0.35, 0.50, 0.14, 0.01 };
  // Order of file popularity within each class
  private static final int FILE_ORDER[] = { 4, 3, 5, 2, 6, 1, 7, 8, 0 };

  // If non-null, always request this URL (unless BOTTLENECK_FREQ is nonzero).
  private static final String STATIC_URL = "/dir00000/class1_7";

  // URL to trigger server bottleneck
  private static final String BOTTLENECK_URL = "/bottleneck";
  // Frequency of bottleneck access
  private static final double BOTTLENECK_FREQ = 0.10;

  // If true, generate special 'X-Persistent' header for Flash web server
  private static final boolean FLASH_HEADERS = false;

  private static Random rand;
  private static URL baseURL;
  private static InetAddress ADDR;
  private static int PORT;
  private static int NUM_CLIENTS;
  private static int REQUEST_DELAY;
  private static int LOAD_CONNECTIONS; 

  private static int numBenchRuns = 0;
  private static HttpLoadThreaded clientThreads[];
  private static Object lock, barrier;
  private static int num_connections = 0;
  private static int num_completions = 0;
  private static long num_bytes = 0L;
  private static int clientNumRequests[];
  private static int clientNumResponses[];
  private static int clientNumErrors[];
  private static boolean timeToQuit = false;

  private int clientnum;
  private String url = null;
  private boolean benchthread;
  private boolean firstConnection;

  private static StatsGatherer connStats, respStats, combinedRespStats;
  private long last_bench_time;

  // Used to format numbers for URL generation
  private static DecimalFormat df;
  static {
    df = new DecimalFormat();
    df.applyPattern("00000");
  }
  private static String format(int val) {
    return df.format((long)val);
  }

  /************************************************************************/

  public HttpLoadThreaded(int clientnum) {
    this.clientnum = clientnum;
    this.benchthread = false;
  }

  public HttpLoadThreaded() {
    this.benchthread = true;
  }

  /************************************************************************/

  // Setup table of Zipf distribution values according to given size
  private static double[] setupZipf(int size) {
    double table[] = new double[size+1];
    double zipf_sum;
    int i;

    for (i = 1; i <= size; i++) {
      table[i] = (double)1.0 / (double)i;
    }

    zipf_sum = 0.0;
    for (i = 1; i <= size; i++) {
      zipf_sum += table[i];
      table[i] = zipf_sum;
    }
    table[size] = 0.0;
    table[0] = 0.0;
    for (i = 0; i < size; i++) {
      table[i] = 1.0 - (table[i] / zipf_sum);
    }
    return table;
  }

  // Set up distribution tables
  private static void setupDists() {
    rand = new Random();

    // Compute number of directories according to SPECweb99 rules
    double opsps = (400000.0 / 122000.0) * LOAD_CONNECTIONS;
    NUMDIRS = (int)(25 + (opsps/5));
    DIR_ZIPF = setupZipf(NUMDIRS);
    FILE_ZIPF = setupZipf(NUMFILES);

    // Sum up CLASS_FREQ table 
    for (int i = 1; i < CLASS_FREQ.length; i++) {
      CLASS_FREQ[i] += CLASS_FREQ[i-1];
    }
  }

  // Return index into Zipf table of random number chosen from 0.0 to 1.0
  private static int zipf(double table[]) {
    double r = rand.nextDouble();
    int i = 0;
    while (r < table[i]) {
      i++;
    }
    return i-1;
  }

  private String chooseURL() {
    double d = rand.nextDouble();
    if ((BOTTLENECK_FREQ > 0) && (d <= BOTTLENECK_FREQ)) {
      if (FLASH_HEADERS) {
	return "GET "+BOTTLENECK_URL+" HTTP/1.1\r\nHost: "+baseURL.getHost()+"\r\nX-Persistent: 1\r\n\r\n";
      } else {
	return "GET "+BOTTLENECK_URL+" HTTP/1.1\r\nHost: "+baseURL.getHost()+"\r\n\r\n";
      }
    }

    if (STATIC_URL != null) {
      if (FLASH_HEADERS) {
	return "GET "+STATIC_URL+" HTTP/1.1\r\nHost: "+baseURL.getHost()+"\r\nX-Persistent: 1\r\n\r\n";
      } else {
	return "GET "+STATIC_URL+" HTTP/1.1\r\nHost: "+baseURL.getHost()+"\r\n\r\n";
      }
    }

    int dir = zipf(DIR_ZIPF);
    int file = FILE_ORDER[ zipf(FILE_ZIPF) ];

    int theclass = 0;
    d = rand.nextDouble();
    while (d > CLASS_FREQ[theclass]) theclass++;

    String request;
    if (FLASH_HEADERS) {
      request = "GET "+baseURL.getPath()+"/dir"+(format(dir))+"/class"+theclass+"_"+file+" HTTP/1.1\r\nHost: "+baseURL.getHost()+"\r\nX-Persistent: 1\r\n\r\n";
    } else {
      request = "GET "+baseURL.getPath()+"/dir"+(format(dir))+"/class"+theclass+"_"+file+" HTTP/1.1\r\nHost: "+baseURL.getHost()+"\r\n\r\n";
    }
    return request;
  }


  /************************************************************************/

  private static void resetStats() {
    connStats = new StatsGatherer("Connect time", "CT", CONN_HIST_BUCKETSIZE);
    respStats = new StatsGatherer("Response time", "RT", RESP_HIST_BUCKETSIZE);
    combinedRespStats = new StatsGatherer("Total response time", "CRT", RESP_HIST_BUCKETSIZE);
//    connStats.reset();
//    respStats.reset();
//    combinedRespStats.reset();
  }

  private void doReport() {
    long num_bts;
    StatsGatherer myConnStats, myRespStats, myCombinedRespStats;

    long cur_time = System.currentTimeMillis();
    double secondsPassed = (cur_time - last_bench_time) * 1.0e-3;

    synchronized (lock) {
      myConnStats = connStats;
      myRespStats = respStats;
      myCombinedRespStats = combinedRespStats;

      System.err.println("BT: "+num_bytes+" "+secondsPassed);
      num_bts = num_bytes; num_bytes = 0L;

      resetStats();
      last_bench_time = System.currentTimeMillis();

      // At this point, we have taken a "snapshot" of the statistics
    }

    long num_conns = myConnStats.num;
    long num_comps = myRespStats.num;
    long max_conn = (long)myConnStats.maxVal;
    long max_resp = (long)myRespStats.maxVal;
    long max_cresp = (long)myCombinedRespStats.maxVal;
    double avg_conn_time = myConnStats.mean();
    double avg_resp_time = myRespStats.mean();
    double avg_cresp_time = myCombinedRespStats.mean();

    double conns_per_sec = (double)num_conns*1.0 / secondsPassed;
    double comps_per_sec = (double)num_comps*1.0 / secondsPassed;
    double bytes_per_sec = (double)(num_bts*1.0) / secondsPassed;

    System.err.println("Connect Rate:\t"+MDWUtil.format(conns_per_sec)+" connections/sec, "+num_conns+" conns");
    System.err.println("Overall rate:\t"+MDWUtil.format(comps_per_sec)+" completions/sec");
    System.err.println("Bandwidth:\t"+MDWUtil.format(bytes_per_sec)+" bytes/sec");

    System.err.println("Connect Time:\t"+MDWUtil.format(avg_conn_time)+" ms, max "+MDWUtil.format(max_conn)+" ms");
    System.err.println("Response Time:\t"+MDWUtil.format(avg_resp_time)+" ms, max "+MDWUtil.format(max_resp)+" ms");
    System.err.println("Combined Response Time:\t"+MDWUtil.format(avg_cresp_time)+" ms, max "+MDWUtil.format(max_cresp)+" ms");

    if (HISTOGRAM_REPORT) {
      myConnStats.dumpHistogram();
      myRespStats.dumpHistogram();
      myCombinedRespStats.dumpHistogram();
    }

  }

  private void doBenchThread() {

    // Wait until first client gets going
    synchronized (barrier) {
      try {
	barrier.wait();
      } catch (InterruptedException ie) {
	System.err.println("WARNING! doBenchThread interrupted...");
      }
    }

    last_bench_time = System.currentTimeMillis();
    while (true) {
      MDWUtil.sleep(BENCH_DELAY);

      if (REPORT) {
	doReport();
      }

      numBenchRuns++;

      if (numBenchRuns == NUMBER_RUNS) {

	System.err.println("Benchmark waiting for clients to quit...\n");
	timeToQuit = true;
	for (int i = 0; i < NUM_CLIENTS; i++) {
	  try {
	    clientThreads[i].join(MAX_JOIN_TIME);
	  } catch (InterruptedException ie) {
	    // Ignore
	  }
	}

	if (REPORT) {
	  System.err.println("Fairness report:");
	  synchronized (lock) {
	    int totalSent = 0, totalReceived = 0, totalErrors = 0;
	    double avgSent, avgReceived, avgErrors, errorRate;
	    for (int i = 0; i < NUM_CLIENTS; i++) {
	      totalSent += clientNumRequests[i];
	      totalReceived += clientNumResponses[i];
	      totalErrors += clientNumErrors[i];
	    }
	    avgSent = (totalSent * 1.0) / (NUM_CLIENTS * 1.0);
	    avgReceived = (totalReceived * 1.0) / (NUM_CLIENTS * 1.0);
	    avgErrors = (totalErrors * 1.0) / (NUM_CLIENTS * 1.0);
	    errorRate = (totalErrors * 1.0) / (totalSent * 1.0);
	    System.err.println("Requests sent: "+totalSent+" total, "+avgSent+" average per client");
	    System.err.println("Responses received: "+totalReceived+" total, "+avgReceived+" average per client");
	    System.err.println("Errors: "+totalErrors+" total, "+avgErrors+" average per client");
	    System.err.println("Error Rate: "+errorRate+" errors per request");
	    for (int i = 0; i < NUM_CLIENTS; i++) {
	      System.err.println("Client "+i+" "+clientNumRequests[i]+" sent, "+clientNumResponses[i]+" received");
	    }
	  }
	}

	System.err.println("Benchmark Finished.\n");
	return;
      }

    }
  }

  /************************************************************************/

  class connectionState {
    Socket sock;
    BufferedReader br;
    LineNumberReader lnr;
    PrintWriter pw;

    connectionState(Socket sock) throws IOException {
      this.sock = sock;
      this.br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      this.lnr = new LineNumberReader(br);
      this.pw = new PrintWriter(new BufferedOutputStream(sock.getOutputStream()));
    }

    void close() {
      try {
	sock.close();
      } catch (Exception e) {
	// Ignore
      }
    }
  }

  private connectionState openConnection() throws IOException {
    long tconn1 = 0, tconn2 = 0;
    boolean connected = false;
    Socket sock = null;

    while (!connected) {
      try {
	tconn1 = System.currentTimeMillis();
	sock = new Socket(ADDR,PORT);
	tconn2 = System.currentTimeMillis();
	connected = true;
      } catch (java.net.NoRouteToHostException nrhe) {
	// Connection timed out!
	continue;
      } catch (IOException e) {
	throw e;
      }
    }

    synchronized (lock) {
      num_connections++;
    }

    if (REPORT) {
      long conntime = tconn2 - tconn1;
      connStats.add(conntime);
    }

    return new connectionState(sock);
  }

  private void randomSleep(int baseTime) {
    if (baseTime > 0) {
      Random r = new Random();
      int st = Math.abs(r.nextInt()) % baseTime;
      System.err.println("Client thread "+clientnum+" sleeping for "+(baseTime+st)+" ms...");
      MDWUtil.sleep(baseTime+st);
    }
  }

  private void doRequest(connectionState conn, boolean newURL) 
    throws IOException {
    if (DEBUG) System.err.println("*** Sending request");
    if (newURL) url = chooseURL();
    conn.pw.print(url);
    conn.pw.flush();
    synchronized(lock) {
      clientNumRequests[clientnum]++;
    }

    int header_size = 0;
    boolean header_seen = false;
    int content_length = 0;
    while (!header_seen) {
      String s = conn.lnr.readLine();
      if (s == null) throw new IOException("Header read returned null");
      header_size += s.length();
      if (DEBUG) System.err.println("Read: "+s+" (length="+s.length()+")");
      if (s.length() == 0) {
	header_seen = true;
	break;
      }
      if (s.startsWith("Content-Length: ")) {
	content_length = Integer.parseInt(s.substring(16, s.length()));
	if (DEBUG) System.err.println("Got content_length "+content_length);
      }
    }

    if (content_length == 0) {
      throw new IOException("Did not get content length");
    }

    int c, count = 0;
    char content[] = new char[content_length];
    while (count < content_length) {
      c = conn.lnr.read(content, count, (content_length - count));
      if (c < 0) throw new IOException("Socket closed");
      count += c;
    }

    //String s = new String(content);
    //if (!s.trim().startsWith(new Integer(content_length).toString())) {
    //  System.err.println("***** ******* ***** BAD RESPONSE: "+s);
    // }
    // if (DEBUG) System.err.println("Got data: "+(new String(content,0,80)));

    synchronized(lock) {
      clientNumResponses[clientnum]++;
      num_completions++; 
      num_bytes += (long)(content_length+header_size);
    }

  }

  private void doPersistent() {
    long tconn1 = 0;                // Time before connection
    long treq1 = 0, treq2 = 0;      // Request time
    boolean newConnection = false;  // Was a new connection established?
    connectionState conn = null;
    int numRequests = 0;
    boolean newURL = true; // Choose new URL for this request
    boolean clientClose = false; // Whether close forced by client

    firstConnection = true;

    if (timeToQuit) {
      System.err.println("Client thread "+clientnum+" quitting...");
      return;
    }

    // Sleep first to let all client start up OK
    randomSleep(CONNECT_DELAY);

    if (timeToQuit) {
      return;
    }

    try {
      tconn1 = System.currentTimeMillis();
      conn = openConnection();
      newConnection = true;

    } catch (Exception e) {
      System.err.println(clientnum+" Got exception trying to connect: "+e);
      e.printStackTrace();
      return;
    }

    if (timeToQuit) {
      System.err.println("Client thread "+clientnum+" quitting...");
      if (conn != null) conn.close();
      return;
    }

    // Sleep to let all clients establish their connections
    randomSleep(CONNECT_DELAY);

    // Wake up the benchThread
    synchronized (barrier) {
      barrier.notify();
    }

    System.err.println("Client thread "+clientnum+" Starting run...");

    while (true) {

      if (timeToQuit) {
	System.err.println("Client thread "+clientnum+" quitting...");
	if (conn != null) conn.close();
	return;
      }

      try { 
	clientClose = false;
	treq1 = System.currentTimeMillis();
	doRequest(conn, newURL);
	treq2 = System.currentTimeMillis();
	newURL = true;

	if (REPORT) {
	  long resptime = treq2 - treq1;

	  long combined_resptime;
	  if (newConnection) {
	    if (firstConnection) {
	      combined_resptime = resptime;
	    } else {
	      combined_resptime = treq2 - tconn1;
	    }
	    newConnection = false;
	  } else {
	    combined_resptime = resptime;
	  }
	  respStats.add(resptime);
	  combinedRespStats.add(combined_resptime);
	}

	if (REQUEST_DELAY > 0) { MDWUtil.sleep(REQUEST_DELAY); }

	if ((MAX_REQS_PER_CONN != -1) && 
	    (numRequests++ == MAX_REQS_PER_CONN)) {
	  // Close the connection and reopen
	  numRequests = 0;
	  clientClose = true;
	  conn.close();
	  throw new IOException("Connection closed by client after maxReqs");
	}

      } catch (Exception e) {
        if (DEBUG) System.err.println(clientnum+" got exception: "+e);

	try {
	  conn.close();
	} catch (Exception e2) {
	  // Ignore
	}

	if (!(e instanceof IOException)) {
	  if (!DEBUG) System.err.println(clientnum+" got exception: "+e);
	  e.printStackTrace();
	}

	if (!clientClose) {
	  // Connection closed by server -- count as error
	  System.err.println("Client "+clientnum+": Connection closed prematurely by server");
	  newURL = false;
	  synchronized (lock) {
	    clientNumErrors[clientnum]++;
	  }
	}

	if (timeToQuit) {
  	  System.err.println("Client thread "+clientnum+" quitting...");
	  if (conn != null) conn.close();
  	  return;
   	}

	if (DEBUG) System.err.println("Trying to reopen socket...");
        try {
	  firstConnection = false;
	  tconn1 = System.currentTimeMillis();
	  conn = openConnection();
	  newConnection = true;
	  if (DEBUG) System.err.println("Connection reestablished");
        } catch (Exception e2) {
          System.err.println(clientnum+" Got exception trying to reconnect: "+e2);
          e2.printStackTrace();
          return;
        }
      }

    } // while (true)
  }

  /************************************************************************/



  /************************************************************************/

  public void run() {
    if (benchthread) { 
      doBenchThread(); 
      System.err.println("benchthread run() returning");
      System.exit(0);
    } else {
      doPersistent();
      System.err.println("client run() returning");
    }
    System.err.println("run() returning");
  }

  /************************************************************************/

  private static void usage() {
    System.err.println("usage: HttpLoadThreaded <baseurl> <numclients> <request delay (ms)> <total connection load> <number of runs>");
    System.exit(1);
  }

  public static void main(String args[]) {

    if (args.length != 5) usage();

    try {
      baseURL = new URL(args[0]);
      ADDR = InetAddress.getByName(baseURL.getHost());
      PORT = baseURL.getPort();
      if (PORT == -1) PORT = 80;

      NUM_CLIENTS = Integer.decode(args[1]).intValue();
      REQUEST_DELAY = Integer.decode(args[2]).intValue();
      LOAD_CONNECTIONS = Integer.decode(args[3]).intValue();
      NUMBER_RUNS = Integer.decode(args[4]).intValue();

      System.err.println("HttpLoadThreaded: Base URL "+baseURL+", "+NUM_CLIENTS+" clients, "+REQUEST_DELAY+" ms delay, "+LOAD_CONNECTIONS+" total load connections, "+NUMBER_RUNS+" runs");

      setupDists();
      System.err.println("Number of directories: "+NUMDIRS);

      lock = new Object();
      barrier = new Object();

      if (REPORT) {
	resetStats();

	HttpLoadThreaded benchthread = new HttpLoadThreaded();
	clientNumRequests = new int[NUM_CLIENTS];
	clientNumResponses = new int[NUM_CLIENTS];
	clientNumErrors = new int[NUM_CLIENTS];
	benchthread.start();
      }

      clientThreads = new HttpLoadThreaded[NUM_CLIENTS];
      for (int i = 0; i < NUM_CLIENTS; i++) {
        clientThreads[i] = new HttpLoadThreaded(i);
      }
      for (int i = 0; i < NUM_CLIENTS; i++) {
        clientThreads[i].start();
      }

    } catch (Exception e) {
      System.err.println("main() got exception: "+e);
      e.printStackTrace();
    }
    System.err.println("main() returning");
  }

}

/************************************************************************/

class statsGatherer {

  private Hashtable histogram;
  private int bucketSize;
  private String name;
  private String tag;
  private double mean = -1;

  private int skip = 0;
  long num = 0;
  long maxVal = 0;
  long cumulativeVal = 0;

  statsGatherer(String name, String tag, int bucketSize) {
    this.name = name;
    this.tag = tag;
    this.bucketSize = bucketSize;
    if (bucketSize != 0) {
      histogram = new Hashtable(1);
    }
  }

  synchronized void add(long val) {
    if (skip < HttpLoadThreaded.SKIP_SAMPLES) {
      skip++;
      return;
    }

    num++;

    if (val > maxVal) maxVal = val;
    cumulativeVal += val;

    if (bucketSize != 0) {
      Integer ct = new Integer((int)val / bucketSize);
      Integer bval = (Integer)histogram.remove(ct);

      if (bval == null) {
	histogram.put(ct, new Integer(1));
      } else {
	bval = new Integer(bval.intValue() + 1);
	histogram.put(ct, bval);
      }
    }
  }

  synchronized void dumpHistogram() {
    Enumeration e = histogram.keys();
    while (e.hasMoreElements()) {
      Integer bucket = (Integer)e.nextElement();
      int time = bucket.intValue() * bucketSize;
      int val = ((Integer)histogram.get(bucket)).intValue();
      System.err.println(tag+" "+time+" ms "+val+" count");
    }
    System.err.println("\n");
  }

  double mean() {
    if (num == 0) return 0.0;
    return (cumulativeVal * 1.0)/num;
  }

}


