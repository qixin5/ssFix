package repair;

import util.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class Main
{
    private static Options options;

    static {
	options = new Options();
	options.addOption("bugid", true, "Bug ID");
	options.addOption("dependjpath", true, "The Dependency Jar Path");
	options.addOption("projdpath", true, "Faulty Project's Directory Path");
	options.addOption("projsrcdpath", true, "Faulty Project's Source Directory Path");
	options.addOption("projbuilddpath", true, "Faulty Project's Build Directory Path (where the binaries for the source files are saved)");
	options.addOption("projtestbuilddpath", true, "Faulty Project's Test Build Directory Path (where the binaries for the test source files are saved)");
	options.addOption("tsuitefpath", true, "The File Containing the Class Names of All the Test Cases (Separated by Semi-colon)");
	options.addOption("tpackage", true, "The package for fault localization");
	options.addOption("failedtestcases", true, "The Full Class Name of the Failed Test Cases (if more than one exist, put them together connected by colons)");
	options.addOption("outputdpath", true, "Output Directory");
	options.addOption("cockerdpath", true, "Cocker Directory Path");
	options.addOption("ssfixdpath", true, "ssFix Directory Path"); 
	options.addOption("maxfaultylines", true, "The Maximum Number of Faulty Lines to be Looked at for Repair");
	options.addOption("maxcandidates", true, "The Maximum Number of Candidate Chunks to be Looked at for Repair");
	options.addOption("analysismethod", true, "The Cocker Search Method");
	options.addOption("faulocfpath", true, "The Path of the Fault Localization Result File");
	options.addOption("faulocaddstacktrace", false, "Use the Stack Trace Information for Fault Localization?");
	options.addOption("usesearchcache", false, "Use Cached Search Result?");

	options.addOption("useextendedcodebase", false, "Use Extended Code Database (including Manually Retrieved Projects from GitHub)?");
	options.addOption("runparallel", false, "Run in parallel?");
	options.addOption("deletefailedpatches", false, "Delete Failed Patches?");
    }

    public static void main(String[] args) {

	CommandLineParser clparser = new DefaultParser();
	CommandLine cmd_line = null;
	try { cmd_line = clparser.parse(options, args); }
	catch (ParseException exp) {
	    System.err.println("CommandLine Parsing Failed: " + exp);
	}
	if (cmd_line == null) { return; }

	String bugid=null, outputdpath=null, projdpath=null, projsrcdpath=null, ssfixdpath=null, projtestbuilddpath=null, tsuitefpath=null;

	if (cmd_line.hasOption("bugid")) {
	    String value = cmd_line.getOptionValue("bugid");
	    System.out.println("Bug ID: " + value);
	    Global.bugid = value;
	    bugid = value;
	}
	else {
	    System.out.println("Bug ID is Not Available.");
	    return;
	}

	if (cmd_line.hasOption("dependjpath")) {
	    String value = cmd_line.getOptionValue("dependjpath");
	    System.out.println("Dependency Jar Path: " + value);
	    Global.dependjpath = value;
	}
	else {
	    System.out.println("Dependency Jar Path is Not Available.");
	    return;
	}

	if (cmd_line.hasOption("projdpath")) {
	    String value = cmd_line.getOptionValue("projdpath");
	    System.out.println("Project Directory Path: " + value);
	    projdpath = value;
	    Global.projdpath = value;
	}
	else {
	    System.out.println("Project Directory Path is Not Available.");
	    return;
	}

	if (cmd_line.hasOption("projsrcdpath")) {
	    String value = cmd_line.getOptionValue("projsrcdpath");
	    System.out.println("Project Source Directory Path: " + value);
	    projsrcdpath = value;
	    Global.projsrcdpath = value;
	}
	else {
	    System.out.println("Project Source Directory Path is Not Available.");
	    return;
	}

	if (cmd_line.hasOption("projbuilddpath")) {
	    String value = cmd_line.getOptionValue("projbuilddpath");
	    System.out.println("Project Build Directory Path: " + value);
	    Global.projbuilddpath = value;
	}
	else {
	    System.out.println("Project Build Directory Path is Not Available.");
	    return;
	}

	if (cmd_line.hasOption("projtestbuilddpath")) {
	    projtestbuilddpath = cmd_line.getOptionValue("projtestbuilddpath");
	    System.out.println("Project Test Build Directory Path: " + projtestbuilddpath);
	    Global.projtestbuilddpath = projtestbuilddpath;
	}
	else {
	    System.out.println("Project Test Build Directory Path is Not Available.");
	    return;
	}

	//If unavailable, ssFix will load failed classes from the fauloc output.
	if (cmd_line.hasOption("failedtestcases")) {
	    String value = cmd_line.getOptionValue("failedtestcases");
	    System.out.println("The Full Class Name of the Failed Test Case(s): " + value);
	    Global.failedtestcases = value;
	}
	else {
	    System.out.println("The Full Class Name of the Failed Test Case(s): " + Global.failedtestcases);
	}
	
	if (cmd_line.hasOption("outputdpath")) {
	    String value = cmd_line.getOptionValue("outputdpath");
	    System.out.println("Output Directory Path: " + value);
	    outputdpath = value;
	    Global.outputdpath = value;
	}
	else {
	    System.out.println("Output Directory Path is Not Available.");
	    return;
	}

	if (cmd_line.hasOption("cockerdpath")) {
	    String value = cmd_line.getOptionValue("cockerdpath");
	    System.out.println("Cocker Directory Path: " + value);
	    Global.cockerdpath = value;
	}
	else {
	    System.out.println("Cocker Directory Path: " + Global.cockerdpath);
	}

	if (cmd_line.hasOption("ssfixdpath")) {
	    String value = cmd_line.getOptionValue("ssfixdpath");
	    System.out.println("ssFix Directory Path: " + value);
	    ssfixdpath = value;
	    Global.ssfixdpath = value;
	}
	else {
	    System.out.println("ssFix Directory Path is Not Available.");
	    return;
	}	
	
	if (cmd_line.hasOption("maxfaultylines")) {
	    String value = cmd_line.getOptionValue("maxfaultylines");
	    System.out.println("The Maximum Number of Faulty Lines to be Considered for Repair " + value);
	    Global.maxfaultylines = Integer.parseInt(value);
	}
	else {
	    System.out.println("The Maximum Number of Faulty Lines to be Considered for Repair: " + Global.maxfaultylines);
	}

	if (cmd_line.hasOption("maxcandidates")) {
	    String value = cmd_line.getOptionValue("maxcandidates");
	    System.out.println("The Maximum Number of Candidates to be Used for Repair: " + value);
	    Global.maxcandidates = Integer.parseInt(value);
	}
	else {
	    System.out.println("The Maximum Number of Candidates to be Used for Repair: " + Global.maxcandidates);
	}

	if (cmd_line.hasOption("analysismethod")) {
	    String value = cmd_line.getOptionValue("analysismethod");
	    System.out.println("Cocker Analysis Method: " + value);
	    Global.analysismethod = value;
	}
	else {
	    System.out.println("Cocker Analysis Method: " + Global.analysismethod);
	}

	//If unavailable, ssFix will do fault localization later.
	if (cmd_line.hasOption("faulocfpath")) {
	    String value = cmd_line.getOptionValue("faulocfpath");
	    System.out.println("The Path of Fault Localization Result File: " + value);
	    Global.faulocfpath = value;
	}
	else {
	    System.out.println("The Path of Fault Localization Result File: " + Global.faulocfpath);
	}

	if (cmd_line.hasOption("usesearchcache")) {
	    System.out.println("Use Search Cache? " + true);
	    Global.usesearchcache = true;
	}
	else {
	    System.out.println("Use Search Cache? " + Global.usesearchcache);
	}

	if (cmd_line.hasOption("useextendedcodebase")) {
	    System.out.println("Use the Extended Code Database? " + true);
	    Global.useextendedcodebase = true;
	}
	else {
	    System.out.println("Use the Extended Code Database? " + Global.useextendedcodebase);
	}
	
	if (cmd_line.hasOption("runparallel")) {
	    System.out.println("Run the Repair in Parallel " + true);
	    Global.runparallel = true;
	}
	else {
	    System.out.println("Run the Repair in Parallel " + Global.runparallel);
	}

	if (cmd_line.hasOption("deletefailedpatches")) {
	    System.out.println("Delete Failed Patches " + true);
	    Global.deletefailedpatches = true;
	}
	else {
	    System.out.println("Delete Failed Patches? " + Global.deletefailedpatches);
	}
		
	File outputdir = new File(outputdpath);
	if (!outputdir.exists()) {
	    System.err.println("Output Directory Not Found: " + outputdpath);
	    return;
	}
	File ssfixdir = new File(ssfixdpath);
	if (!ssfixdir.exists()) {
	    System.err.println("ssFix Directory Not Found: " + ssfixdpath);
	    return;
	}

	String output_bugid_dpath = outputdpath+"/"+bugid;
	File output_bugid_dir = new File(output_bugid_dpath);
	if (!output_bugid_dir.exists()) { output_bugid_dir.mkdir(); }
	
	if (cmd_line.hasOption("tsuitefpath")) {
	    tsuitefpath = cmd_line.getOptionValue("tsuitefpath");
	    System.out.println("Test Suite File Path: " + tsuitefpath);
	    Global.tsuitefpath = tsuitefpath;
	}
	else {
	    //Generate a test suite file including all the test classes in the binary test directory
	    tsuitefpath = output_bugid_dpath + "/testsuite_classes";
	    File tsuitef = new File(tsuitefpath);
	    if (!tsuitef.exists()) {
		String[] path_convert_cmds = new String[]{
		    "ant", "-f", "pathconverter.xml",
		    "-Dtestbuilddir="+projtestbuilddpath,
		    "-Doutputdir="+output_bugid_dpath,
		    "-Doutputfname=testsuite_classes",
		    "write_tsuitefile"
		};
		CommandExecutor.execute(path_convert_cmds, ssfixdir);
	    }
	    System.out.println("Test Suite File Path: " + tsuitefpath);
	    Global.tsuitefpath = tsuitefpath;
	}

	System.out.println();
	long start_time = System.currentTimeMillis();
	Repair repair = new Repair();
	repair.repair(bugid, projdpath, projsrcdpath, outputdpath);
	long end_time = System.currentTimeMillis();
	System.out.println("Repair execution time: " + (end_time - start_time));
    }
}
