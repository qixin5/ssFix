package freenet.node.rt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import freenet.Core;
import freenet.FieldSet;
import freenet.Key;
import freenet.support.Unit;
import freenet.support.HexUtil;
import freenet.support.Logger;
import freenet.support.graph.Color;
import freenet.support.graph.GDSList;
import freenet.support.graph.GraphDataSet;


/**
 * Similar to SlidingBucketsKeyspaceEstimator, but uses doubles instead of
 * BigIntegers to represent keys. These two should be kept strictly up to date
 * with one another - please do a diff between the two and check that the only 
 * changes are changes from BigInteger ops to double ops, whenever you change
 * SBKE or this file. 
 */
public class FastSlidingBucketsKeyspaceEstimator extends NumericKeyKeyspaceEstimator implements DoubleKeyspaceEstimator {

    private static final int SERIAL_MAGIC = 0x2534;
    
    private static final boolean WRITE_AS_OTHER = false;

    public class SlidingBucketsHTMLReportingTool extends StandardHTMLReportTool {

        /**
         * @param estimator
         */
        public SlidingBucketsHTMLReportingTool(
                NumericKeyKeyspaceEstimator estimator) {
            super(estimator);
        }

        protected void dumpHtmlMiddle(PrintWriter pw) {
            if(logDEBUG)
                dump("dumpHtmlMiddle", Logger.DEBUG, false);
            pw.println("<tr><th>Key</th><th>Raw value</th><th>Smoothed value</th><th>Reports</th><th>Detail</th>");
            synchronized(this) { // REDFLAG: we ARE writing into a stringwriter, right?
                for(int i=0;i<accuracy;i++) {
                    pw.println("<tr><td>");
                    pw.println(dumpKey(leftEdge[i]));
                    pw.println("</td><td>");
                    pw.println(type.rawToString(ra[i].currentValue()));
                    pw.println("</td><td>");
                    pw.println(type.rawToString(getBucketValue(i)));
                    pw.println("</td><td>");
                    pw.println(ra[i].countReports());
                    pw.println("</td><td>");
                    pw.println(ra[i].toString());
                    pw.println("</td></tr>\n");
                }
            }
        }

        protected void dumpLog() {
        }

        protected int columnCount() {
            return 5;
        }

    }

    /**
     * Dump a key
     */
    private String dumpKey(double d) {
        return Double.toString(d);
    }

    /*
     * leftEdge are the boundaries.
     * centers are the centers.
     * So ra[i] serves the sector formed by boundaries 
     * leftEdge[i] and leftEdge[i+1] with center
     * centers[i].
     * KEPT IN STRICTLY INCREASING ORDER.
     */
    
    final RunningAverage[] ra;
    final double[] leftEdge;
    final double[] centers;
    final RecentReports recent;
    final double movementFactor;
    final int accuracy;
    final int TARGET_REPORTS = 10;
    final boolean doSmoothing;

    final double KEYSPACE_SIZE = Key.KEYSPACE_SIZE.doubleValue();
    
    /** Construct blank, with equally spaced dividing keys, and initial value of 0
     * for RunningAverage's.
     * @param factory RunningAverageFactory to use for the RunningAverage's.
     * @param accuracy the number of sectors. Each is bounded by two keys, and
     * has a value represented by a RunningAverage. 
     */
    public FastSlidingBucketsKeyspaceEstimator(RunningAverageFactory factory, int accuracy,
            double movementFactor, double initValue, Unit type, String name, boolean doSmoothing) {
        super(type, name);
        this.movementFactor = movementFactor;
        this.accuracy = accuracy;
        this.doSmoothing = doSmoothing;
        ra = new RunningAverage[accuracy];
        leftEdge = new double[accuracy];
        centers = new double[accuracy];
        // Initial dividing keys: gap should be keyspace / accuracy
		double a = KEYSPACE_SIZE - 1;
		double b = a / accuracy;
		double c = b/2;
        for(int i=(accuracy-1);i>=0;i--) {
            a = a - b;
            ra[i] = factory.create(initValue);
            leftEdge[i] = a;
            centers[i] = a + c;
        }
        recent = new RecentReports();
        if(Core.logger.shouldLog(Logger.MINOR, this))
            dump("Initialized flat", Logger.MINOR, false);
    }

    // Serialization to disk and to FieldSet: version 2.
    // Version 1 had lots of bugs!
    
    /**
     * Create a FastSlidingBucketsKeyspaceEstimator from one that has been
     * serialized to disk.
     * @param raf RunningAverageFactory to use for the RunningAverage's.
     * @param accuracy the number of sectors. Each is bounded by two keys, and
     * has a value represented by a RunningAverage.
     * @param mf
     * @param dis the stream to read from
     * @param maxValue the maximum permitted value 
     * @param minValue the minimum permitted value
     */
    public FastSlidingBucketsKeyspaceEstimator(RunningAverageFactory raf, int accuracy,
            double mf, DataInputStream dis, Unit type, String name, boolean doSmoothing) 
            throws IOException {
        super(type, name);
        this.movementFactor = mf;
        int magic = dis.readInt();
        this.doSmoothing = doSmoothing;
        if(magic != SERIAL_MAGIC)
            throw new IOException("Invalid magic");
        int ver = dis.readInt();
        if(ver != 2)
            throw new IOException("Unrecognized version: "+ver+" - possible format change");
        int acc = dis.readInt();
        if(acc != accuracy)
            throw new IOException("Accuracy changed");
        this.accuracy = accuracy;
        centers = new double[accuracy];
        leftEdge = new double[accuracy];
        ra = new RunningAverage[accuracy];
        for(int i=0;i<accuracy;i++) {
            centers[i] = readBigInteger(dis).doubleValue();
            leftEdge[i] = readBigInteger(dis).doubleValue();
            if(i != 0 && leftEdge[i] > centers[i])
                throw new IOException("Border greater than center on "+i);
            ra[i] = raf.create(dis);
            if(!type.withinRange(ra[i].currentValue()))
                throw new IOException("Value out of range: "+ra[i].currentValue()+" from "+ra[i]+" should be "+type.getMin()+"< x <"+type.getMax());
        }
        recent = new RecentReports();
        correctSlide();
        String s = checkConsistency();
        if(s != null)
            throw new IOException("Serialized inconsistent (and uncorrectable) estimator: "+s);
    }

    /**
     * @param rafSmooth
     * @param set
     * @param minAllowedTime
     * @param maxAllowedTime
     */
    public FastSlidingBucketsKeyspaceEstimator(RunningAverageFactory raf, double mf, int accuracy,
            FieldSet set, Unit type, String name, boolean doSmoothing) 
    	throws EstimatorFormatException {
        super(type, name);
        if(logDEBUG)
            Core.logger.log(this, "Serializing from: "+set, Logger.DEBUG);
        this.doSmoothing = doSmoothing;
        if(set == null) throw new EstimatorFormatException("null FieldSet");
		String impl = set.getString("Implementation");
		if (impl == null)
			throw new EstimatorFormatException("no Implementation in RunningAverage");
		boolean fakeOther = impl.equals("SlidingBucketsKeyspaceEstimator");
		if((!fakeOther) && !impl.equals("FastSlidingBucketsKeyspaceEstimator"))
			throw new EstimatorFormatException(
				"unknown implementation " + impl);
		String v = set.getString("Version");
		if (v == null || !v.equals("2"))
			throw new EstimatorFormatException("Invalid version " + v);
		// Now read it in
		String l = set.getString("Accuracy");
		int acc;
		try {
		    acc = Integer.parseInt(l,16);
		} catch (NumberFormatException e) {
		    throw new EstimatorFormatException("Invalid Accuracy: "+l);
		}
		if(acc != accuracy)
		    throw new EstimatorFormatException("Different accuracy: "+acc+" should be "+accuracy);
		this.accuracy = accuracy;
		ra = new RunningAverage[accuracy];
		leftEdge = new double[accuracy];
		centers = new double[accuracy];
		recent = new RecentReports();
		movementFactor = mf;
		FieldSet points = set.getSet("Points");
		if(points == null)
		    throw new EstimatorFormatException("No points!");
		if(logDEBUG)
		    Core.logger.log(this, "Points: "+points, Logger.DEBUG);
		for(int i=0;i<accuracy;i++) {
		    FieldSet point = points.getSet(Integer.toHexString(i));
		    if(point == null) throw new EstimatorFormatException("No point "+Integer.toHexString(i)+", full fieldset="+set);
		    ra[i] = raf.create(point.getSet("Value"));
            if(!type.withinRange(ra[i].currentValue()))
                throw new EstimatorFormatException("Value out of range: "+ra[i].currentValue()+" from "+ra[i]+" should be "+type.getMin()+"< x <"+type.getMax());

		    String key = point.getString("DividingKey");
		    if(key == null) throw new EstimatorFormatException("No key "+i);
		    if(fakeOther) {
		        leftEdge[i] = new BigInteger(key, 16).doubleValue();
		    } else {
		        try {
		            leftEdge[i] = Double.parseDouble(key);
		        } catch (NumberFormatException e) {
		            throw new EstimatorFormatException("Invalid key: "+key);
		        }
		    }
		    if(leftEdge[i] < 0.0)
		        throw new EstimatorFormatException("Negative key: "+leftEdge[i]+" for "+i);
		    if(leftEdge[i] > KEYSPACE_SIZE)
		        throw new EstimatorFormatException("Too big key: "+leftEdge[i]+" for "+i);
		}
		/** 
		 * Minimum sector size: 
		 * 1/10th of the size of one sector if all are equal size.
		 * This means about 40 hits in that sector at 5% movementFactor.
		 * The point here is to prevent fake passed in estimators from taking an
		 * excessively long time to correct.
		 */
		double minStep = KEYSPACE_SIZE / (accuracy * 10);
		// Run loop twice because center[] all null until updateCenter called.
		for(int i=0;i<accuracy;i++) {
		    updateCenter(i);
		}
		for(int i=0;i<accuracy;i++) {
		    double thisCenter = centers[i];
		    int prev = (i + accuracy - 1) % accuracy;
		    double prevCenter = centers[prev];
		    if(thisCenter < prevCenter) {
		        // thisCenter > prevCenter
		        thisCenter += KEYSPACE_SIZE;
		    }
		    double diff = thisCenter - prevCenter;
		    if(diff < minStep)
		        throw new EstimatorFormatException("Overspecialized probably fake estimator: refusing to accept FieldSet: diff "+i+"="+diff);
		}
		correctSlide();
        String s = checkConsistency();
        if(s != null)
            throw new EstimatorFormatException("INCONSISTENT: "+s);
    }

    /**
     * @param estimator
     */
    public FastSlidingBucketsKeyspaceEstimator(FastSlidingBucketsKeyspaceEstimator e) {
        super(e);
        this.accuracy = e.accuracy;
        this.centers = (double[]) e.centers.clone();
        this.doSmoothing = e.doSmoothing;
        this.leftEdge = (double[]) e.leftEdge.clone();
        this.movementFactor = e.movementFactor;
        this.ra = new RunningAverage[e.ra.length];
        for(int i=0;i<ra.length;i++) {
            ra[i] = (RunningAverage) e.ra[i].clone();
        }
        this.recent = (RecentReports) e.recent.clone();
    }

    /**
     * Read a (reasonably short) BigInteger from a DataInputStream
     * @param dis the stream to read from
     * @return a BigInteger
     */
    private BigInteger readBigInteger(DataInputStream dis) throws IOException {
        short i = dis.readShort();
        if(i < 0) throw new IOException("Invalid BigInteger length: "+i);
        byte[] buf = new byte[i];
        dis.readFully(buf);
        return new BigInteger(1,buf);
    }

    public HTMLReportTool getHTMLReportingTool() {
        return new SlidingBucketsHTMLReportingTool(this);
    }

    public synchronized double lowestRaw() {
        double lowest = type.getMax();
        for(int i=0;i<accuracy;i++) {
            double d = getBucketValue(i);
            if(d < lowest) lowest = d;
        }
        return lowest;
    }

    public double lowest() {
        return type.ofRaw(lowestRaw());
    }
    
    public synchronized double highestRaw() {
        double highest = 0;
        for(int i=0;i<accuracy;i++) {
            double d = getBucketValue(i);
            if(d > highest) highest = d;
        }
        return highest;
    }

    public double highest() {
        return type.ofRaw(highestRaw());
    }
    
	public String lowestString() {
		return type.rawToString(lowestRaw());
	}

	public String highestString() {
		return type.rawToString(highestRaw());
	}
	
    public GDSList createGDSL(int samples,
            boolean drawHistoryIfPossible, Color lineCol) {
        GDSList gdsl = new GDSList();
        if(logDEBUG)
            dump("Creating GDSL", Logger.DEBUG, false);
        GraphDataSet gds = this.createGDS(samples, 0);
        gdsl.add(gds, lineCol);
        return gdsl;
    }

    public FieldSet toFieldSet() {
        FieldSet fs = new FieldSet();
        if(WRITE_AS_OTHER) {
            fs.put("Implementation", "SlidingBucketsKeyspaceEstimator");
            fs.put("Version", "2");
        } else {
            fs.put("Implementation", "FastSlidingBucketsKeyspaceEstimator");
            // Theoretically 1 but easier to start at 2 :)
            // If either changes we need to change the from-network constructor 
            // to check version differently depending on impl
            fs.put("Version", "2");
        }
        fs.put("Accuracy", Integer.toHexString(accuracy));
        FieldSet points = new FieldSet();
        fs.put("Points", points);
        synchronized(this) {
            for(int i=0;i<accuracy;i++) {
                FieldSet pt = new FieldSet();
                points.put(Integer.toHexString(i), pt);
                pt.put("Value", ra[i].toFieldSet());
                if(WRITE_AS_OTHER) {
                   pt.put("DividingKey", HexUtil.biToHex(toBigInteger(leftEdge[i])));
                } else {
                    pt.put("DividingKey", Double.toString(leftEdge[i]));
                }
            }
        }
        return fs;
    }

    public void writeDataTo(DataOutputStream out) throws IOException {
        out.writeInt(SERIAL_MAGIC);
        out.writeInt(2);
        out.writeInt(accuracy);
        synchronized(this) {
            for(int i=0;i<accuracy;i++) {
                writeBigInteger(toBigInteger(centers[i]), out);
                writeBigInteger(toBigInteger(leftEdge[i]), out);
                ra[i].writeDataTo(out);
            }
        }
    }

    private BigInteger toBigInteger(double d) {
        BigDecimal bd = new BigDecimal(d);
        return bd.toBigInteger();
    }

    /**
     * Write a (reasonably short) BigInteger to a stream.
     * @param integer the BigInteger to write
     * @param out the stream to write it to
     */
    private void writeBigInteger(BigInteger integer, DataOutputStream out) throws IOException {
        if(integer.signum() == -1) {
            dump("Negative BigInteger", Logger.ERROR, true);
            throw new IllegalStateException("Negative BigInteger!");
        }
        byte[] buf = integer.toByteArray();
        if(buf.length > Short.MAX_VALUE)
            throw new IllegalStateException("Too long: "+buf.length);
        out.writeShort((short)buf.length);
        out.write(buf);
    }

    public int getDataLength() {
        int l = 4 + 4;
        for(int i=0;i<accuracy;i++) {
            synchronized(this) {
                l += 2 + toBigInteger(centers[i]).toByteArray().length;
                l += 2 + toBigInteger(centers[i]).toByteArray().length;
                l += ra[i].getDataLength();
            }
        }
        return l;
    }

    public RecentReports recentReports() {
        return recent;
    }

    public double guessRaw(Key k) {
        return guess(k.toDouble(),0);
    }

    public double guessRaw(Key k, int age) {
        return guess(k.toDouble(), 0);
    }
    
    public double guessTime(Key k) {
        checkType(TIME, false);
        return guess(k.toDouble(),0);
    }

    public double guessProbability(Key k) {
        checkType(PROBABILITY, false);
        double d = guess(k.toDouble(),0);
        if(d < 0.0) {
            Core.logger.log(this, "Guessed probability: "+d+" on "+this,
                    Logger.ERROR);
            return 0.0;
        }
        if(d > 1.0) {
            Core.logger.log(this, "Guessed probability: "+d+" on "+this,
                    Logger.ERROR);
            return 1.0;
        }
        return d;
    }

    public double guessTransferRate(Key k) {
        checkType(TRANSFER_RATE, false);
        return guess(k.toDouble(),0);
    }

    public void reportTime(Key k, long millis) {
        checkType(TIME, true);
        report(k.toDouble(), millis);
    }

    /**
     * Check that the type of this estimator is the same as the report
     * or guess. Grumble if not.
     * @param t the expected type.
     * @param b is this a report? otherwise is a guess request.
     */
    private void checkType(Unit t, boolean b) {
        if(t != type) {
            Core.logger.log(this, "Type wrong on "+(b ? "reporting" : "guessing") +
                    ": "+type(t)+" should be "+type(), new Exception("debug"), Logger.ERROR);
        }
    }

    public void reportProbability(Key k, double p) {
        checkType(PROBABILITY, true);
        report(k.toDouble(), p);
    }

    public void reportTransferRate(Key k, double rate) {
        checkType(TRANSFER_RATE, true);
        report(k.toDouble(), rate);
    }

    protected Object getGDSSync() {
        return this;
    }

    double guess(BigInteger ok, int age) {
        return guess(ok.doubleValue(), age);
    }
    
    /**
     * Guess a value for a given key.
     * Algorithm:
     * Find the two nearest sectors, by their centers.
     * Get their values.
     * Interpolate between them.
     * @param k the key to guess a value for
     * @param age ignored
     */
    double guess(double k, int age) {
        logDEBUG = Core.logger.shouldLog(Logger.DEBUG, this);
//        if(logDEBUG)
//            Core.logger.log(this, "Guessing: "+HexUtil.biToHex(k)+" on "+this,
//                    Logger.DEBUG);
        double firstKeyAfter = -1;
        double firstKeyBefore = -1;
        double origK = k;
        double beforeValue = -1;
        double afterValue = -1;
        synchronized(this) {
            int idx = java.util.Arrays.binarySearch(centers, k);
            if(idx >= 0) {
                // Exact match! (rather unlikely)
                return getBucketValue(idx);
            } else {
                idx = (-idx)-1;
                // idx is now the insertion point.
                // so if it is 0, the key is smaller than the first entry
                // and if it is accuracy, it is larger than the last entry
                // so the first key after is idx
                // and the first key before is idx-1
                int after = idx;
                if(after >= accuracy) after -= accuracy; // cheaper than %, right?
                int before = idx-1;
                if(before < 0) before += accuracy;
                firstKeyAfter = centers[after];
                afterValue = getBucketValue(after);
                firstKeyBefore = centers[before];
                beforeValue = getBucketValue(before);
//                if(logDEBUG) {
//                    Core.logger.log(this, "After: "+after+": "+ra[after]+" = "+afterValue, Logger.DEBUG);
//                    Core.logger.log(this, "Before: "+before+": "+ra[before]+" = "+beforeValue, Logger.DEBUG);
//                }
            }
        }
        if(firstKeyBefore > firstKeyAfter) {
            if(k < firstKeyAfter)
                k += KEYSPACE_SIZE;
            firstKeyAfter = firstKeyAfter + KEYSPACE_SIZE;
        }
        // Found sector
        // Now just interpolate
        double interpolatedValue = interpolate(k, firstKeyBefore, beforeValue, firstKeyAfter, afterValue);

        if(/*logDEBUG || */interpolatedValue < 0.0) {
            String s = interpolatedValue+
            	" in guess("+dumpKey(origK)+" -> "+dumpKey(k)+
            	") on "+this+": keyBefore="+dumpKey(firstKeyBefore)+
            	", keyAfter="+dumpKey(firstKeyAfter)+", key="+k+
            	", beforeValue="+beforeValue+", afterValue="+afterValue;
//            if(interpolatedValue < 0.0) {
                Core.logger.log(this, "Interpolated crazy value: "+s, Logger.ERROR);
//            }
//            if(logDEBUG)
//                Core.logger.log(this, "Interpolated: "+s, Logger.DEBUG);
        }
        return interpolatedValue;
    }

    /**
     * Calculates, given the other input values, the interpolated value of 'key'.
     */
    protected double interpolate(double key, double firstKeyBefore, double beforeValue, double firstKeyAfter, double afterValue) {
        double bigdiff = firstKeyAfter - firstKeyBefore;
        double smalldiff = firstKeyAfter - key;

        double p = smalldiff / bigdiff;
        double interpolatedValue =
            afterValue + p * (beforeValue - afterValue);
		return interpolatedValue;
	}

	/**
     * Get the current value of a bucket. Bootstrapping phase
     * smoothing occurs here.
     * @param index the index of the bucket to retrieve.
     * @return the current value of the given bucket.
     */
    private double getBucketValue(int index) {
        if(!doSmoothing) {
            return ra[index].currentValue();
        } else {
        double centerValue = ra[index].currentValue();
        // FIXME: make a parameter
        // Tried 25, think 10 may work better..
        long coreReports = ra[index].countReports();
        if(coreReports > TARGET_REPORTS) return centerValue;
        int totalReports = (int)coreReports;
        double weightedTotal = totalReports * centerValue;
        int before = index-1;
        int after = index+1;
        int prevBefore = Integer.MIN_VALUE;
        int prevAfter = Integer.MIN_VALUE;
//        if(logDEBUG)
//            Core.logger.log(this, "getBucketValue("+index+"): centerValue="+
//                    centerValue+", coreReports="+coreReports+", weightedTotal="+
//                    weightedTotal+", totalReports="+totalReports, Logger.DEBUG);
        while(true) {
            if(before == -1) before += accuracy;
            if(after == accuracy) after -= accuracy;
            boolean beforeDone = false;
            boolean afterDone = false;
            if(before == index && after == index) break;
            if(before == prevAfter && after == prevBefore) break;
            int neededReports = TARGET_REPORTS - totalReports;
//            if(logDEBUG)
//                Core.logger.log(this, "after="+after+", before="+before+
//                        ", neededReports="+neededReports, Logger.DEBUG);
            if(neededReports <= 0) break;
            double innerTotal = 0.0;
            int innerReports = 0;
            if(before != index && before != prevAfter) {
                int reports = (int)ra[before].countReports();
                innerReports += reports;
                innerTotal += (ra[before].currentValue() * reports);
            } else {
                beforeDone = true; 
            }
            if(after != index && after != prevBefore 
                    && after != before /* if they converge we want one value only */) {
                int reports = (int)ra[after].countReports();
                innerReports += reports;
                innerTotal += (ra[after].currentValue() * reports);
            } else {
                afterDone = true;
            }
            if(beforeDone && afterDone) break;
            if(neededReports >= innerReports) {
                totalReports += innerReports;
                weightedTotal += innerTotal;
            } else {
                totalReports += neededReports;
                weightedTotal += (innerTotal * neededReports) / innerReports;
            }
//            if(logDEBUG)
//                Core.logger.log(this, "innerReports: "+innerReports+
//                        ", innerTotal: "+innerTotal+", weightedTotal: "+
//                        weightedTotal+", totalReports: "+totalReports+
//                        ", beforeDone="+beforeDone+", afterDone="+afterDone, 
//                        Logger.DEBUG);
            if(before == after) break;
            prevBefore = before;
            prevAfter = after;
            before--;
            after++;
        }
        if(totalReports == 0) return centerValue; // return init value
//        if(logDEBUG)
//            Core.logger.log(this, "Returning "+weightedTotal/totalReports,
//                    Logger.DEBUG);
        return weightedTotal / totalReports;
        }
    }

    void report(BigInteger ok, double d) {
        recent.report(ok, d);
        double k = ok.doubleValue();
        innerReport(k, d);
    }
    
    private void innerReport(double k, double d) {
        logDEBUG = Core.logger.shouldLog(Logger.DEBUG, this);
        synchronized(this) {
            errorCheckConsistency();
            if(logDEBUG)
                dump("Before report("+dumpKey(k)+","+d+")", Logger.DEBUG, true);
            // Search CENTERS, not boundaries, because CENTERS ARE KEPT IN
            // ORDER !
            int sidx = java.util.Arrays.binarySearch(centers, k);
            
            if(sidx >= 0) {
                // Exact match on center sidx
                // Which is between boundaries sidx and sidx+1
                moveTowardsAndUpdateCentersNotDeadOn(k, sidx, d);
                return;
            } else {
                // Rather more complicated!
                int idx = -sidx-1;
                if(idx == accuracy) {
                    // We are after center # accuracy-1, at the right hand end
                    if(leftEdge[0] < leftEdge[1]) {
                        // leftEdge[0] < leftEdge[1]
                        // We are at the other end, so we are in [accuracy-1]
                        moveTowardsAndUpdateCentersNotDeadOn(k, accuracy-1, d);
                        return;
                    }
                } else if(idx == 0) {
                    // We are behind center #0
                    if(leftEdge[0] > centers[0]) {
                        // leftEdge[0] is on the other end so we are 
                        // definitely in sector 0
                        moveTowardsAndUpdateCentersNotDeadOn(k, 0, d);
                        return;
                    }
                }
                idx = idx % accuracy;
                int idxMinusOne = (idx + accuracy - 1) % accuracy;
                if(leftEdge[idx] > k) {
                    // leftEdge[idx] > k => we are in sector before.
                    // Not wrapped around because of above special cases.
                    moveTowardsAndUpdateCentersNotDeadOn(k, idxMinusOne, d);
                } else if(leftEdge[idx] < k) {
                    // We are in sector idx
                    moveTowardsAndUpdateCentersNotDeadOn(k, idx, d);
                } else {
                    // leftEdge[idx] == k - we are dead on
                    moveTowardsAndUpdateCentersDeadOn(k, idx, d);
                }
            }
//            int idx = sidx;
//            int before;
//            int after;
//            int affectedSector1;
//            int affectedSector2;
//            int sectorWhoseRAIsAffected;
//            if(idx >= 0) {
//                // Exact match on the border
//                // Border stays where it is
//                // Move the borders towards the key
//                before = (idx + accuracy - 1) % accuracy;
//                after = (idx + 1) % accuracy;
//                affectedSector1 = (before + accuracy - 1) % accuracy;
//                affectedSector2 = (after + 1) % accuracy;
//                sectorWhoseRAIsAffected = idx;
//            } else {
//                idx = -idx-1;
//                idx = idx % accuracy;
//                before = (idx + accuracy - 1) % accuracy;
//                after = idx % accuracy;
//                affectedSector1 = (accuracy + before - 1) % accuracy;
//                affectedSector2 = -1;
//                /** Insert point is idx.
//                 * idx-1 is first key before.
//                 * idx is first key after.
//                 * Move the border closer to k of idx-1 towards k,
//                 * and the border closer to k of idx towards k.
//                 * The sector the key landed in is "before", because
//                 * as explained above, the boundary is the START of the
//                 * sector X, the center is then after it, in the middle of
//                 * sector X, and the RA for X covers that sector:
//                 * See the comments before the declarations.
//                 */
//                 sectorWhoseRAIsAffected = before;
//            }
//            moveDividingKey(before, k);
//            if(logDEBUG)
//                dump("After moved before", Logger.DEBUG, false);
//            moveDividingKey(after, k);
//            if(logDEBUG)
//                dump("After moved after", Logger.DEBUG, false);
//            updateCenters(before, after, affectedSector1, affectedSector2);
//            if(logDEBUG)
//                dump("After fixed centers", Logger.DEBUG, false);
//            ra[idx].report(d);
//            correctSlide();
//            
//            
//            errorCheckConsistency();
//            if(logDEBUG)
//                dump("After report("+HexUtil.biToHex(k)+","+d+"): sidx="+sidx+
//                        ", idx="+idx+", before="+before+", after="+after+
//                        ", affected="+affectedSector1+" and "+affectedSector2, 
//                        Logger.DEBUG, false);
        }
    }

    /**
     * @param k The reported key
     * @param sector The sector the report falls into. 
     * @param d The value of the actual report.
     */
    private void moveTowardsAndUpdateCentersNotDeadOn(double k, int sector, double d) {
        // We are between leftEdge[sector] and leftEdge[sector+1]
        int nextSector = sector + 1;
        int nextPlusSector = sector + 2;
        int prevSector = sector - 1;
        if(prevSector < 0) prevSector += accuracy;
        if(nextSector >= accuracy) nextSector -= accuracy;
        if(nextPlusSector >= accuracy) nextPlusSector -= accuracy;
        moveTowardsKDirectional(k, sector, true);
        moveTowardsKDirectional(k, (sector + 1) % accuracy, false);
        ra[sector].report(d);
        updateCenter(sector);
        updateCenter(nextSector);
        updateCenter(prevSector);
        updateCenter(nextPlusSector);
        correctSlide();
        errorCheckConsistency();
        if(logDEBUG)
            dump("Reported "+dumpKey(k)+":"+d+
                    " via moveTowardsAndUpdateCentersNotDeadOn(k,"+sector+
                    ",d): nextSector="+nextSector+", prevSector="+prevSector+
                    ", nextPlusSector="+nextPlusSector, Logger.DEBUG, false);
    }

    /**
     * @param sector The sector whose left edge we are to move.
     * @param forward If true, move in the positive direction only. If
     * false, move in the negative direction only.
     */
    private void moveTowardsKDirectional(double k, int sector, boolean forward) {
        double edge = leftEdge[sector];
        double diff = edge - k;
        /**
         * If we are going forward, then we expect k > edge => diff<0
         * If we are going backward, then we expect edge > k => diff>0
         */
        if(forward) diff = -diff;
        if(diff < 0) {
            /**
             * Going forward, k < edge:
             * diff > 0
             * Then diff < 0 (invert).
             * Now diff < 0 so we are here.
             * Adding a keyspace will make us go forward.
             */
            /** 
             * Going backward, k > edge:
             * diff < 0
             * Then diff < 0 (don't invert).
             * We are pointed in the right direction but by the wrong amount.
             * If we add a keyspace we get the right amount.
             */
            diff += KEYSPACE_SIZE;
        } else {
            /**
             * Going forward, k > edge:
             * diff < 0
             * Invert: diff > 0, so we are here.
             * Right size, right direction.
             */
            /**
             * Going backwards, k < edge:
             * diff > 0
             * diff > 0 because no invert.
             * We are pointed in the wrong direction but we are the right size.
             */
        }
        if(!forward) diff = -diff;
        
        // Now diff is the right direction and the right size
        // Now scale it.
        
        double bd = diff * movementFactor;
        double result = edge + bd;
        
        if(result < 0) result = result + KEYSPACE_SIZE;
        if(result > KEYSPACE_SIZE)
            result -= KEYSPACE_SIZE;
        leftEdge[sector] = result;
    }

    /**
     * Similar to above, but we hit a border dead-on.
     * @param k The key being reported
     * @param sector The sector whose left boundary the key is equal to.
     * @param d The value being reported.
     */
    private void moveTowardsAndUpdateCentersDeadOn(double k, int sector, double d) {
        // leftEdge[sector] should be unchanged
        int nextSector = sector + 1;
        int prevSector = sector - 1;
        int pprevSector = sector - 2;
        if(prevSector < 0) prevSector += accuracy;
        if(pprevSector < 0) pprevSector += accuracy;
        if(nextSector >= accuracy) nextSector -= accuracy;
        moveTowardsKDirectional(k, prevSector, true);
        moveTowardsKDirectional(k, nextSector, false);
        ra[sector].report(d);
        updateCenter(sector);
        updateCenter(prevSector);
        updateCenter(nextSector);
        updateCenter(pprevSector);
        correctSlide();
        errorCheckConsistency();
        if(logDEBUG)
            dump("Reported "+dumpKey(k)+":"+d+
                    " via moveTowardsAndUpdateCentersNotDeadOn(k,"+sector+
                    ",d): nextSector="+nextSector+", prevSector="+prevSector+
                    ", nextPlusSector="+pprevSector, Logger.DEBUG, false);
    }

    /**
     * Correct any overall slide that occurred after a report.
     */
    private void correctSlide() {
        /** It may have wrapped one way or the other.
         * Either a:
         *  Wrapped backwards: [0] is 0xf..., just above [15]
         * Or b:
         *  Wrapped forwards: [15] is 0x1..., just below [0]
         */
        int count = 0;
        while(count < accuracy) {
            if(centers[0] > centers[1]) {
                count++;
                // centers[0] > centers[1]
                // [0] -> [15], [1] -> [0], [2] -> [1], ...
                double oldCenter = centers[0];
                double oldBorder = leftEdge[0];
                RunningAverage oldRA = ra[0];
                System.arraycopy(centers, 1, centers, 0, accuracy-1);
                System.arraycopy(leftEdge, 1, leftEdge, 0, accuracy-1);
                System.arraycopy(ra, 1, ra, 0, accuracy-1);
                centers[accuracy-1] = oldCenter;
                leftEdge[accuracy-1] = oldBorder;
                ra[accuracy-1] = oldRA;
                if(logDEBUG)
                    dump("After left shift "+count, Logger.DEBUG, false);
            } else if(centers[accuracy-1] < centers[accuracy-2]) {
                count++;
                // centers[15] < centers[14]
                // [15] -> [0], [0] -> [1], [1] -> [2], ...
                double oldCenter = centers[accuracy-1];
                double oldBorder = leftEdge[accuracy-1];
                RunningAverage oldRA = ra[accuracy-1];
                System.arraycopy(centers, 0, centers, 1, accuracy-1);
                System.arraycopy(leftEdge, 0, leftEdge, 1, accuracy-1);
                System.arraycopy(ra, 0, ra, 1, accuracy-1);
                centers[0] = oldCenter;
                leftEdge[0] = oldBorder;
                ra[0] = oldRA;
                if(logDEBUG)
                    dump("After right shift "+count, Logger.DEBUG, false);
            } else return;
        }
        Core.logger.log(this, "Too many shifts!: "+count+" for "+this, Logger.ERROR);
    }

    private void errorCheckConsistency() {
        String s = checkConsistency();
        if(s != null)
            dump(s, Logger.ERROR, true);
    }

    /**
     * Check the consistency of the sectors.
     * Consistency definition:
     * centers is strictly ascending.
     * leftEdge is strictly ascending within wraparound.
     * leftEdge[x] <= center[x] <= leftEdge[x+1]
     * (with wraparound)
     */
    private synchronized String checkConsistency() {
        double prevCenter = -1;
        StringBuffer ret = null;
        for(int i=0;i<accuracy;i++) {
            double center = centers[i];
            if(center < 0) {
                if(ret == null) ret = new StringBuffer();
                ret.append("NEGATIVE CENTER: "+dumpKey(center));
            }
            if(center > KEYSPACE_SIZE) {
                if(ret == null) ret = new StringBuffer();
                ret.append("TOO HIGH CENTER: "+dumpKey(center));
            }
            if(prevCenter >= 0 && i != (accuracy-1) &&
                    prevCenter > center) {
                if(ret == null) ret = new StringBuffer();
                ret.append("INCONSISTENT: ["+i+"]: "+dumpKey(center)+
                        " < prev "+dumpKey(prevCenter)+"\n");
            }
            if(prevCenter >= 0 && prevCenter == center) {
                if(ret == null) ret = new StringBuffer();
                ret.append("TWO SECTORS EQUAL CENTERS: "+dumpKey(center)+" at "+i+"\n");
            }
            double boundary = leftEdge[i];
            if(boundary < 0) {
                if(ret == null) ret = new StringBuffer();
                ret.append("NEGATIVE BOUNDARY: "+dumpKey(boundary));
            }
            if(boundary > KEYSPACE_SIZE) {
                if(ret == null) ret = new StringBuffer();
                ret.append("TOO HIGH BOUNDARY: "+dumpKey(boundary));
            }
            if(i != (accuracy-1) && 
                    boundary > center) {
                // boundary > center.
                // Could be due to wraparound, IF it's the first element.
                if(i == 0) continue;
                if(ret == null) ret = new StringBuffer();
                ret.append("INCONSISTENT: boundary["+i+"] > center["+i+"]\n");
            }
            // Don't need to check next boundary because of centers check.
        }
        if(centers[0] > centers[accuracy-1]) {
           if(ret == null) ret = new StringBuffer();
           ret.append("INCONSISTENT: [0] > [end]: [0] = "+dumpKey(centers[0])+
                   ", [end] = "+dumpKey(centers[accuracy-1])+"\n");
        }
        if(centers[0] == centers[accuracy-1]) {
            if(ret == null) ret = new StringBuffer();
            ret.append("INCONSISTENT: [0] == [end] = "+dumpKey(centers[0])+"\n");
        }
        return ret == null ? null : new String(ret);
    }

    /**
     * Log the current status of the sectors, with a mesage.
     * @param message the message to log with the dump.
     * @param prio the log priority to log at
     */
    private void dump(String message, int prio, boolean stackTrace) {
        StringBuffer sb = new StringBuffer();
        sb.append(message);
        sb.append(" for ");
        sb.append(super.toString());
        sb.append(" dump:\n");
        for(int i=0;i<accuracy;i++) {
            sb.append(i);
            sb.append(": start ");
            sb.append(dumpKey(leftEdge[i]));
            sb.append(" center ");
            sb.append(dumpKey(centers[i]));
            sb.append(" value ");
            sb.append(ra[i].toString());
            sb.append('\n');
        }
        if(stackTrace)
            Core.logger.log(this, sb.toString(), new Exception("debug"), prio);
        else
            Core.logger.log(this, sb.toString(), prio);
    }

    private synchronized void updateCenter(int c1) {
        double lowerEdge = leftEdge[c1];
        double upperEdge = leftEdge[(c1 + 1) % accuracy];
        if(upperEdge > lowerEdge) {
            // lowerEdge < upperEdge, all is normal, no wraparound
            centers[c1] = (lowerEdge + upperEdge)/2;
        } else {
            // upperEdge < lowerEdge, need to wrap around
            upperEdge += KEYSPACE_SIZE;
            double movedTo = (lowerEdge + upperEdge)/2;
            if(movedTo > KEYSPACE_SIZE)
                movedTo -= KEYSPACE_SIZE;
            centers[c1] = movedTo;
        }
    }

    public int maxAge() {
        return 0;
    }

    protected double guessRaw(BigInteger b, int age) {
        return guess(b, age);
    }

    public int countReports() {
        int totalReports = 0;
        for(int i=0;i<accuracy;i++) {
            totalReports += Math.max(0,ra[i].countReports());
        }
        return totalReports;
    }
    
    public boolean noReports() {
        for(int i=0;i<accuracy;i++) {
            if(ra[i].countReports() > 0) return false;
        }
        return true;
    }

    /**
     * @param bd
     */
    public void getBucketDistribution(BucketDistribution bd) {
        long max = 0;
        long min = Integer.MAX_VALUE;
        bd.setAccuracy(accuracy);
        for(int i=0;i<accuracy;i++) {
            long x = ra[i].countReports();
            bd.buckets[i] = x;
            bd.vals[i] = getBucketValue(i);
            bd.ras[i] = ra[i].toString();
            BigDecimal centerDec = new BigDecimal(centers[i]);
            BigInteger centerInt = centerDec.toBigInteger();
            bd.center[i] = new Key(centerInt);
            max = Math.max(x, max);
            min = Math.min(x, min);
        }
        bd.maxBucketReports = max;
        bd.minBucketReports = min;
    }

    public double guess(double k) {
        return guess(k, 0);
    }

    public void report(double k, double d) {
        innerReport(k, d); // FIXME: Won't report on reports
    }

    public void report(double k, boolean d) {
        innerReport(k, d ? 1.0 : 0.0);
    }

    public double guess(Double k) {
        return guess(k.doubleValue(), 0);
    }

    public void report(Double k, double d) {
        innerReport(k.doubleValue(), d);
    }

    public void report(Double k, boolean d) {
        innerReport(k.doubleValue(), d ? 1.0 : 0.0);
    }

    public void reportProbability(Double k, double d) {
        innerReport(k.doubleValue(), d);
    }

    public void reportProbability(double k, double d) {
        innerReport(k, d);
    }

    public void reportTime(Double k, double d) {
        innerReport(k.doubleValue(), d);
    }

    public void reportTime(double k, double d) {
        innerReport(k, d);
    }

    public Object clone() {
        return new FastSlidingBucketsKeyspaceEstimator(this);
    }
}
