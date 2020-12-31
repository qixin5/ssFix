package edu.brown.cs.ssfix.repair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.apache.commons.io.FileUtils;
import edu.brown.cs.ssfix.search.*;
import edu.brown.cs.ssfix.patchgen.*;
import edu.brown.cs.ssfix.util.*;

public class Repair0
{
    public final int MAX_CHUNK_SIZE_FACTOR = 5;

    private String bug_id;
    private String ssfix_dpath;
    private String cocker_dpath;
    private String anal_method;
    private String proj_dpath;
    private String proj_testbuild_dpath;
    private String dependjpath;
    private String tsuite_fpath;
    private String failed_testcases;
    private int max_candidate_num;
    private int parallel_granularity;
    private boolean use_search_cache;
    private boolean write_search_rslt;
    private boolean localproj_merobase_only;
    private boolean delete_failed_patches;
    private Pattern localproj_merobase_pathptn;
    private SearchInvoker search_invoker;
    
    public Repair0(String bugid, String ssfixdpath, String cockerdpath, String projdpath, String projtestbuilddpath, String dependjpath, String tsuitefpath, String failedtestcases, String analysismethod, int max_candidates, int paragran, boolean usesearchcache, boolean useextendedcodebase, boolean deletefailedpatches) {
	bug_id = bugid;
	ssfix_dpath = ssfixdpath;
	cocker_dpath = cockerdpath;
	proj_dpath = projdpath;
	proj_testbuild_dpath = projtestbuilddpath;
	this.dependjpath = dependjpath;
	tsuite_fpath = tsuitefpath;
	failed_testcases = failedtestcases;
	anal_method = analysismethod;
	max_candidate_num = max_candidates;
	parallel_granularity = paragran;
	use_search_cache = usesearchcache;
	localproj_merobase_only = !useextendedcodebase;
	write_search_rslt = true;
	delete_failed_patches = deletefailedpatches;
	//localproj_merobase_pathptn = Pattern.compile(".+/defects4j-bugs/.+/.+_buggy/.+\\.java|.+/merobase_split_100/.+/srcs/.+\\.java|.+/.+_local_fix\\.java");
	localproj_merobase_pathptn = Pattern.compile(".+/defects4j-bugs/.+/.+_buggy/.+\\.java|.+/merobase_split_100/.+/srcs/.+\\.java");
	search_invoker = new SearchInvoker();
    }

    public Patch repair(String bfpath, String bstmt_loc, String fix_dpath) {
	String cocker_rslt_fpath = fix_dpath+"/cocker_rslt";
	File cocker_rslt_f = new File(cocker_rslt_fpath);
	if (use_search_cache && cocker_rslt_f.exists()) {
	    return repair0(bfpath, bstmt_loc, fix_dpath);
	}
	else {
	    return repair1(bfpath, bstmt_loc, fix_dpath);
	}
    }

    /* Using the cached search result. */
    private Patch repair0(String bfpath, String bstmt_loc, String fix_dpath) {
	//Load the buggy node
	File bf = new File(bfpath);
	String bfctnt = null;
	try { bfctnt = FileUtils.readFileToString(bf); }
	catch (IOException e) { System.err.println(e); e.printStackTrace(); }
	CompilationUnit bcu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(bfpath, bfctnt);
	List<ASTNode> bnode_list = ASTNodeFinder.find(bcu, bstmt_loc);
	if (bnode_list.isEmpty() || bnode_list.get(0) == null) {
	    System.err.println("Cannot find the suspicious statement from "+bfpath+","+bstmt_loc);
	    return new Patch(null, false); }
	ASTNode bnode = bnode_list.get(0);
	
	//Generate the local context search loc
	SearchStringGenerator ssgen = new SearchStringGenerator();
	String search_loc = ssgen.getLocalContextSearchLoc(bnode, bstmt_loc);

	//Try loading cocker search file
	List<String> cchunk_lines = null;
	String cocker_rslt_fpath = fix_dpath+"/cocker_rslt";
	File cocker_rslt_f = new File(cocker_rslt_fpath);
	try { cchunk_lines = FileUtils.readLines(cocker_rslt_f, (String)null); }
	catch (Throwable t) { System.err.println(t); t.printStackTrace(); }
	if (cchunk_lines==null || cchunk_lines.isEmpty()) { cchunk_lines = null; }
	else {
	    String pathloc = cchunk_lines.get(0);
	    if (pathloc.equals(bfpath+","+search_loc)) {
		cchunk_lines.remove(0); //The first line is header
	    }
	    else {
		System.err.println("Inconsistent Path-Locs Found.");
		System.err.println("Path-loc Generated: " + bfpath+","+search_loc);
		System.err.println("Path-loc from Cocker Result File (the first line): " + pathloc);
		cchunk_lines = null;
	    }
	}

	if (cchunk_lines==null || cchunk_lines.isEmpty()) {
	    return new Patch(null, false);
	}
	
	//Repair using each candidate
	int cchunk_num = cchunk_lines.size();
	PatchGenerator pgen = new PatchGenerator(bug_id, ssfix_dpath, proj_dpath, proj_testbuild_dpath, dependjpath, tsuite_fpath, failed_testcases, delete_failed_patches, parallel_granularity);
	String bloc = getChunkLoc(bfpath, bfctnt, search_loc);
	if (bloc == null || "".equals(bloc)) {
	    System.err.println("Cannot get the correct chunk loc from " + search_loc);
	    return new Patch(null, false);
	}
	else {
	    System.out.println("Buggy Chunk Loc: " + bloc); 
	    System.out.println(); 
	}
	List<ASTNode> bnodes = ASTNodeFinder.find(bcu, bloc);
	if (bnodes == null || bnodes.isEmpty()) {
	    return new Patch(null, false); //invalid target
	} 
	BuggyChunk bchunk = new BuggyChunk(bfpath, bloc, bnodes, bfctnt);
	int bchunk_length = bchunk.getLengthInLines();
	int max_cchunk_length = bchunk_length * MAX_CHUNK_SIZE_FACTOR;

	Patch rslt_patch = null;
	boolean patch_found = false;
	int tested_num = 0;
	int cchunk_count = 0;
	for (int i=0; i<cchunk_num; i++) {

	    if (cchunk_count > max_candidate_num) { break; }
	    
	    //Generate the candidate chunk
	    System.out.println("Looking at Candidate Chunk " + i);
	    String cchunk_line = cchunk_lines.get(i);
	    System.out.println(cchunk_line);
	    int i0 = cchunk_line.indexOf(",");
	    int i1 = cchunk_line.lastIndexOf(",");
	    String cfpath = cchunk_line.substring(0, i0);
	    String cloc = cchunk_line.substring(i0+1, i1);
	    if (cfpath.startsWith("file://")) { cfpath = cfpath.substring(7); }


	    //Is the candidate forbidden?
	    if (BlackChecker.isBlack(bug_id, cfpath, cloc)) {
		System.out.println("Forbidden. Skip.");
		continue;
	    }
	    
	    //File cf = new File(cfpath);
	    //if (!cf.exists() || !cf.canRead()) {
	    //System.out.println("Cannot read file: " + cfpath);
	    //continue;
	    //}

	    //String cfctnt = CandidateLoader.getFileContent(cfpath, anal_method);
	    String cfctnt = null;
	    try { cfctnt = FileUtils.readFileToString(new File(cfpath)); }
	    catch (Throwable t) { System.err.println(t); t.printStackTrace(); }
	    if (cfctnt == null || "".equals(cfctnt)) {
		System.out.println("Cannot get the candidate file content from: " + cfpath);
		continue;
	    }
	    CompilationUnit ccu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(cfpath, cfctnt);
	    cloc = getChunkLoc0(ccu, cloc);//Remove the method wrapper
	    if (cloc == null || "".equals(cloc)) {
		System.out.println("Empty chunk, skip.");
		continue;
	    }

	    //Repair bchunk using cchunk
	    System.out.println("Cloc: " + cloc);
	    cchunk_count += 1;
	    List<ASTNode> cnodes = ASTNodeFinder.find(ccu, cloc);
	    if (cnodes == null || cnodes.isEmpty()) {
		continue; //invalid candidate
	    }
	    CandidateChunk cchunk = new CandidateChunk(cfpath, cloc, cnodes, cfctnt);
	    int cchunk_length = cchunk.getLengthInLines();
	    if (cchunk_length <= 0 || cchunk_length >= max_cchunk_length) {
		System.out.println("Empty or too large chunk, skip.");
		continue;
	    }
	    String fix_dpath0 = fix_dpath+"/c"+i;
	    File fix_d0 = new File(fix_dpath0);
	    if (!fix_d0.exists()) { fix_d0.mkdirs(); }
	    
	    Patch patch = pgen.generatePatch(bchunk, cchunk, fix_dpath0);
	    if (patch == null) {
		System.out.println("Candidate Chunk "+i+" Failed.");
	    }
	    else if (patch.isCorrect()) {
		System.out.println("Found Plausible Patch at " + patch.getFilePath());
		System.out.println("Candidate Rank: " + cchunk_count);
		tested_num += patch.getTestedNum();
		patch_found = true;
		rslt_patch = new Patch(patch.getFilePath(), true);
		break;
	    }
	    else {
		System.out.println("Candidate Chunk "+i+" Failed.");
		tested_num += patch.getTestedNum();
	    }
	    System.out.println();
	}

	System.out.println("Tested Patch Num: " + tested_num);
	if (rslt_patch == null) { rslt_patch = new Patch(null, false); }
	rslt_patch.setTestedNum(tested_num);
	return rslt_patch;
    }


    /* Doing code search. */
    private Patch repair1(String bfpath, String bstmt_loc, String fix_dpath) {
	//Load the buggy node
	File bf = new File(bfpath);
	String bfctnt = null;
        try { bfctnt = FileUtils.readFileToString(bf); }
        catch (IOException e) { System.err.println(e); e.printStackTrace(); }
	CompilationUnit bcu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(bfpath, bfctnt);
	List<ASTNode> bnode_list = ASTNodeFinder.find(bcu, bstmt_loc);
	if (bnode_list.isEmpty() || bnode_list.get(0) == null) {
	    System.err.println("Cannot find the suspicious statement from "+bfpath+","+bstmt_loc);
	    return new Patch(null, false); }
	ASTNode bnode = bnode_list.get(0);
	String[] binfo = getClassNameAndMethodSignature(bnode);
	
	//Generate the local context search loc
	SearchStringGenerator ssgen = new SearchStringGenerator();
	String search_loc = ssgen.getLocalContextSearchLoc(bnode, bstmt_loc);

	String cocker_rslt_fpath = fix_dpath+"/cocker_rslt";
	List<String> cchunk_lines = search_invoker.invoke(bfpath, search_loc, ssfix_dpath, anal_method, cocker_rslt_fpath, false); //no filtering is performed
	if (cchunk_lines==null || cchunk_lines.isEmpty()) {
	    System.err.println("No cocker result available.");
	    return new Patch(null, false);
	}
	
	//Repair using each candidate
	int cchunk_num = cchunk_lines.size();
	PatchGenerator pgen = new PatchGenerator(bug_id, ssfix_dpath, proj_dpath, proj_testbuild_dpath, dependjpath, tsuite_fpath, failed_testcases, delete_failed_patches, parallel_granularity);
	String bloc = getChunkLoc(bfpath, bfctnt, search_loc);
	if (bloc == null || "".equals(bloc)) {
	    System.err.println("Cannot get the correct chunk loc from " + search_loc);
	    return new Patch(null, false);
	}
	else {
	    System.out.println("Buggy Chunk Loc: " + bloc);
	    System.out.println();
	}
	List<ASTNode> bnodes = ASTNodeFinder.find(bcu, bloc);
	if (bnodes == null || bnodes.isEmpty()) {
            return new Patch(null, false); //invalid target
	}
	BuggyChunk bchunk = new BuggyChunk(bfpath, bloc, bnodes, bfctnt);
	int bchunk_length = bchunk.getLengthInLines();
	int max_cchunk_length = bchunk_length * MAX_CHUNK_SIZE_FACTOR;

	String curr_str_no_space = null;
	Patch rslt_patch = null;
	boolean patch_found = false;
	int tested_num = 0;
	int cchunk_count = 0;
	for (int i=0; i<cchunk_num; i++) {

	    if (cchunk_count > max_candidate_num) { break; }
	    
	    //Generate the candidate chunk
	    System.out.println("Looking at Candidate Chunk " + i);
	    String cchunk_line = cchunk_lines.get(i);
	    System.out.println(cchunk_line);
	    int i0 = cchunk_line.indexOf(",");
	    int i1 = cchunk_line.lastIndexOf(",");
	    String cfpath = cchunk_line.substring(0, i0);
	    String cloc = cchunk_line.substring(i0+1, i1);
	    if (cfpath.startsWith("file://")) { cfpath = cfpath.substring(7); }

	    //Is the candidate forbidden?
	    if (BlackChecker.isBlack(bug_id, cfpath, cloc)) {
		System.out.println("Forbidden. Skip.");
		continue;
	    }
	    
	    //File cf = new File(cfpath);
	    //if (!cf.exists() || !cf.canRead()) {
	    //System.out.println("Cannot read file: " + cfpath);
	    //continue;
	    //}

	    //String cfctnt = CandidateLoader.getFileContent(cfpath, anal_method);
	    String cfctnt = null;
	    try { cfctnt = FileUtils.readFileToString(new File(cfpath)); }
	    catch (Throwable t) { System.err.println(t); t.printStackTrace(); }
            if (cfctnt == null || "".equals(cfctnt)) {
                System.out.println("Cannot get the candidate file content from: " + cfpath);
                continue;
            }
	    CompilationUnit ccu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(cfpath, cfctnt);
            //cloc = getChunkLoc0(ccu, cloc);//Remove the method wrapper
	    if (cloc == null || "".equals(cloc)) {
		System.out.println("Empty chunk, skip.");
		continue;
	    }
	    List<ASTNode> cnodes = ASTNodeFinder.find(ccu, cloc);
            if (cnodes == null || cnodes.isEmpty()) {
		continue; //invalid candidate
	    }                                    
	    CandidateChunk cchunk = new CandidateChunk(cfpath, cloc, cnodes, cfctnt);
	    List<ASTNode> cchunk_nodes = cchunk.getNodeList();
	    if (cchunk_nodes==null || cchunk_nodes.isEmpty()) {
		System.out.println("Empty chunk, skip.");
		continue;
	    }
	    ASTNode cnode0 = cchunk_nodes.get(0);
	    if (cnode0 == null) {
		System.out.println("Chunk node is null, skip.");
		continue;
	    }
	    if (cnode0 instanceof MethodDeclaration) {
		System.out.println("Chunk is a Method, skip.");
		continue;
	    }
	    String[] cinfo = getClassNameAndMethodSignature(cnode0);
	    if (cinfo[0].equals(binfo[0]) && cinfo[1].equals(binfo[1])) {
		System.out.println("Class Name & Method Signature Identical to BChunk, skip.");
		continue;
	    }
	    int cchunk_length = cchunk.getLengthInLines();
	    if (cchunk_length <= 0 || cchunk_length >= max_cchunk_length) {
		System.out.println("Empty or too large chunk, skip.");
		continue;
	    }
	    String cchunk_str = "";
	    for (ASTNode cchunk_node : cchunk_nodes) {
		if (cchunk_node != null) { cchunk_str += "\n"+cchunk_node.toString(); }
	    }
	    String cchunk_str_no_space = cchunk_str.replaceAll("\\s+","");
	    if (cchunk_str_no_space.equals(curr_str_no_space)) {
		System.out.println("Redundant Chunk, skip.");
		continue;
	    }
	    else {
		curr_str_no_space = cchunk_str_no_space;
	    }

	    //Repair bchunk using cchunk
	    System.out.println("Cloc: " + cloc);
	    cchunk_count += 1;
	    String fix_dpath0 = fix_dpath+"/c"+i;
	    File fix_d0 = new File(fix_dpath0);
	    if (!fix_d0.exists()) { fix_d0.mkdirs(); }
	    
	    Patch patch = pgen.generatePatch(bchunk, cchunk, fix_dpath0);
	    if (patch == null) {
		System.out.println("Candidate Chunk "+i+" Failed.");
	    }
	    else if (patch.isCorrect()) {
		System.out.println("Found Plausible Patch at " + patch.getFilePath());
		System.out.println("Candidate Rank: " + cchunk_count);
		tested_num += patch.getTestedNum();
		patch_found = true;
		rslt_patch = new Patch(patch.getFilePath(), true);
		break;
	    }
	    else {
		System.out.println("Candidate Chunk "+i+" Failed.");
		tested_num += patch.getTestedNum();
	    }
	    System.out.println();
	}

	System.out.println("Tested Patch Num: " + tested_num);
	if (rslt_patch == null) { rslt_patch = new Patch(null, false); }
	rslt_patch.setTestedNum(tested_num);
	return rslt_patch;
    }


    
    /* Remove the nested locs; Use "slc" consistently. */
    private String getChunkLoc(String bfpath, String bfctnt, String search_loc) {
	String rslt = null;
	String[] sublocs = search_loc.split(";");
	for (String subloc : sublocs) {
	    String[] subloc_items = subloc.split(":");
	    if (subloc_items[0].endsWith("-nested")) {
		continue;
	    } else {
		if (rslt == null) { rslt = "slc:"+subloc_items[1]; }
		else { rslt += ";slc:"+subloc_items[1]; }
	    }
	}
	if (rslt == null) { return rslt; }
	else {
	    CompilationUnit bcu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(bfpath, bfctnt);
	    return getChunkLoc0(bcu, rslt); //Could be null (method's no-body)
	}
    }

    /*
    private String getChunkLoc0(String fpath, String loc, String fctnt) {
	CompilationUnit cu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(fpath, fctnt);	
	return getChunkLoc0(cu, loc);
    }
    */

    private String getChunkLoc0(CompilationUnit cu, String loc) {
	List<ASTNode> found_nodes = ASTNodeFinder.find(cu, loc);
	if (found_nodes.size()==1) {
	    ASTNode found_node = found_nodes.get(0);
	    if (found_node instanceof MethodDeclaration) {
		MethodDeclaration md = (MethodDeclaration) found_node;
		Block md_body = md.getBody();
		if (md_body == null) { return null; } //no-body
		List stmt_obj_list = md.getBody().statements();
		String new_loc = null;
		for (Object stmt_obj : stmt_obj_list) {
		    ASTNode stmt = (ASTNode) stmt_obj;
		    int pos = stmt.getStartPosition();
		    String new_subloc = "slc:"+cu.getLineNumber(pos)+","+cu.getColumnNumber(pos);
		    if (new_loc == null) { new_loc = new_subloc; }
		    else { new_loc += ";" + new_subloc; }
		}
		return new_loc;
	    }
	}
	return loc;
    }

    /* If loc corresponds to a method, get the loc for body stmts. 
     flag 0: bchunk
     flag 1: cchunk */
    /*
    private String getChunkLoc0(String fpath, String loc, int flag, String analmethod) {
	List<ASTNode> found_nodes = null;
	CompilationUnit cu = null;
	if (flag == 0) {
	    cu = (CompilationUnit) ASTNodeLoader.getASTNode(new File(fpath));
	    found_nodes = ASTNodeFinder.find(cu, loc);
	}
	else {
	    String cfctnt = CandidateLoader.getFileContent(fpath, analmethod);
	    cu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(fpath, cfctnt);
	    found_nodes = ASTNodeFinder.find(cu, loc);
	}

	if (found_nodes.size()==1) {
	    ASTNode found_node = found_nodes.get(0);
	    if (found_node instanceof MethodDeclaration) {
		MethodDeclaration md = (MethodDeclaration) found_node;
		Block md_body = md.getBody();
		if (md_body == null) { return null; } //no-body
		List stmt_obj_list = md.getBody().statements();
		String new_loc = null;
		for (Object stmt_obj : stmt_obj_list) {
		    ASTNode stmt = (ASTNode) stmt_obj;
		    int pos = stmt.getStartPosition();
		    String new_subloc = "slc:"+cu.getLineNumber(pos)+","+cu.getColumnNumber(pos);
		    if (new_loc == null) { new_loc = new_subloc; }
		    else { new_loc += ";" + new_subloc; }
		}
		return new_loc;
	    }
	}

	return loc;
    }
    */

    private String[] getClassNameAndMethodSignature(ASTNode tnode) {
	String cname = "";
	String msig = "";
	CompilationUnit cu = (CompilationUnit) tnode.getRoot();
	AbstractTypeDeclaration atd = null;
	MethodDeclaration md = null;
	ASTNode currnode = tnode;
	while (currnode != null) {
	    if ((md==null) && (currnode instanceof MethodDeclaration)) {
		md = (MethodDeclaration) currnode; //Find the first enclosing method
	    }
	    if (currnode instanceof AbstractTypeDeclaration) {
		atd = (AbstractTypeDeclaration) currnode; //Find the last enclosing class
	    }
	    currnode = currnode.getParent();
	}
	if (md != null) {
	    msig = getMethodSignatureString(md);
	}
	if (atd != null) {
	    cname = atd.getName().getIdentifier();
	    PackageDeclaration pd = cu.getPackage();
	    if (pd != null) { cname = pd.getName().toString()+"."+cname; }
	}
	if (cname.startsWith("org.apache.commons.lang3")) {
	    cname = cname.replace("org.apache.commons.lang3","org.apache.commons.lang");
	}
	else if (cname.startsWith("org.apache.commons.math3")) {
	    cname = cname.replace("org.apache.commons.math3","org.apache.commons.math");
	}
	return new String[] { cname, msig };
    }

    private String getMethodSignatureString(MethodDeclaration md) {
	String mname = md.getName().getIdentifier();
	String marg = null;
	List param_list = md.parameters();
	for (Object param_obj : param_list) {
	    SingleVariableDeclaration param_svd = (SingleVariableDeclaration) param_obj;
	    if (marg == null) { marg = param_svd.getType().toString(); }
	    else { marg += "$" + param_svd.getType().toString(); }
	}
	if (marg == null) { marg = ""; }
	return mname + "(" + marg + ")";
    }
    
}
