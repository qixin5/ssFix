package edu.brown.cs.ssfix.repair;

public class Global
{
    public static String bugid;
    public static String dependjpath;
    public static String projdpath;
    public static String projsrcdpath;
    public static String projbuilddpath;
    public static String projtestbuilddpath;
    public static String tsuitefpath;
    public static String failedtestcases;
    public static String outputdpath;
    public static String ssfixdpath;
    public static String cockerdpath;
    public static int maxfaultylines;
    public static int maxcandidates;
    public static int parallelgranularity;
    public static String analysismethod;
    public static String faulocfpath;
    public static boolean usesearchcache;
    public static boolean useextendedcodebase;
    public static boolean runparallel;
    public static boolean deletefailedpatches;

    static {
	bugid = null;
	dependjpath = null;
	projdpath = null;
	projsrcdpath = null;
	projbuilddpath = null;
	projtestbuilddpath = null;
	tsuitefpath = null;
	failedtestcases = null;
	outputdpath = null;
	ssfixdpath = null;
	cockerdpath = null;
	maxfaultylines = 30;
	maxcandidates = 100;
	parallelgranularity = 1;
	analysismethod = "k5pprbstmsm";
	faulocfpath = null;
	usesearchcache = false;
	useextendedcodebase = false;
	runparallel = true;
	deletefailedpatches = false;
    }
}
