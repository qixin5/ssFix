package repair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.apache.commons.io.FileUtils;
import patchgen.Patch;
import util.*;
import fauloc.FaultLocalization;


public class Repair
{
    public void repair(String bug_id, String projdpath, String projsrcdpath, String outputdpath) {
	int max_faulty_lines = Global.maxfaultylines;
	int max_candidates = Global.maxcandidates;
	String tsuite_fpath = Global.tsuitefpath; //Shouldn't be null at this point
	String fauloc_fpath = Global.faulocfpath; //fauloc_fpath is null if fauloc file was not provided by user
	String anal_method = Global.analysismethod;
	boolean use_search_cache = Global.usesearchcache;
	boolean use_extended_codebase = Global.useextendedcodebase;
	boolean delete_failed_patches = Global.deletefailedpatches;
	String cocker_dpath = Global.cockerdpath;
	String ssfix_dpath = Global.ssfixdpath;
	String proj_dpath = Global.projdpath;
	String proj_testbuild_dpath = Global.projtestbuilddpath;
	String dependjpath = Global.dependjpath;
	String failed_testcases = Global.failedtestcases;

	File fauloc_f = null;
	List<String> fauloc_lines = null;
	if (fauloc_fpath == null) { //If so, set as the DEFAULT fpath
	    fauloc_fpath = outputdpath+"/"+bug_id+"/gzoltar_fauloc";
	}
	fauloc_f = new File(fauloc_fpath);

	if (!fauloc_f.exists()) {
	    System.out.println("No Fault Localization Result Found!");
	}
	else {
	    System.out.println("Loading Fault Localization Result from File: " + fauloc_fpath + " ...");
	    try { fauloc_lines = FileUtils.readLines(fauloc_f, (String)null); }
	    catch (Throwable t) { System.err.println(t); t.printStackTrace(); }
	}

	if (fauloc_lines != null) {
	    System.out.println("Fault Localization Done.");
	}
	else {
	    System.err.println("Fault Localization Failure.");
	    return;
	}

	if (failed_testcases == null) {
	    System.out.println("Loading failed test cases from fauloc result ...");
	    for (String fauloc_line : fauloc_lines) {
		String fauloc_line0 = fauloc_line.trim();
		if (fauloc_line0.startsWith("Test failed:")) {
		    int index0 = "Test failed:".length();
		    int index1 = fauloc_line0.indexOf("#");
		    String failed_testcase = ((index1==-1) ? fauloc_line0.substring(index0) : fauloc_line0.substring(index0, index1)).trim();
		    if (failed_testcases == null) { failed_testcases = failed_testcase; }
		    else {
			if (!failed_testcases.contains(failed_testcase)) {
			    failed_testcases += ":" + failed_testcase;
			}
		    }
		}
		if (fauloc_line0.startsWith("Suspicious line:")) { break; }
	    }
	    System.out.println("Loading failed test cases from fauloc result Done.");
	    System.out.println("Failed test cases: " + failed_testcases);
	}

	if (failed_testcases == null) {
	    System.err.println("Nothing Failed.");
	    return;
	}
	
	System.err.println("Looking at each located faulty line ...");
	Repair0 repair0 = new Repair0(bug_id, ssfix_dpath, cocker_dpath, proj_dpath, proj_testbuild_dpath, dependjpath, failed_testcases, anal_method, max_candidates, use_search_cache, use_extended_codebase, delete_failed_patches);
	String fix_dpath0 = outputdpath+"/"+bug_id+"/patches";
	File fix_dir0 = new File(fix_dpath0);
	if (!fix_dir0.exists()) { fix_dir0.mkdirs(); }
	boolean patch_found = false;
	int tested_num = 0;
	List<PathLoc> pathloc_list = FaultFetcher.fetch(projsrcdpath, fauloc_lines, max_faulty_lines);
	int pathloc_list_size = pathloc_list.size();
	for (int i=0; i<pathloc_list_size; i++) {
	    PathLoc pathloc = pathloc_list.get(i);
	    String bfpath = pathloc.getPath();
	    String bstmt_loc = pathloc.getLoc();
	    String fix_dpath = fix_dpath0+"/f"+i;
	    System.out.println("===== Looking at Faulty Statement No."+i+" (" + bfpath + "," + bstmt_loc + ") =====");
	    Patch patch0 = repair0.repair(bfpath, bstmt_loc, fix_dpath);
	    tested_num += (patch0.getTestedNum()<0) ? 0 : patch0.getTestedNum();
	    if (patch0.isCorrect()) {
		patch_found = true;
		break;
	    }
	    System.out.println("Current total tested patch num: " + tested_num);
	}

	if (!patch_found) { System.out.println("Repair Failed."); }
	System.out.println("Total Tested Patch Num: " + tested_num);
    }
}
