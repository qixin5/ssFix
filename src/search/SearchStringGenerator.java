package search;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import util.*;


public class SearchStringGenerator
{
    public final int MAX_LINES0 = 6;
    public final int MAX_LINES1 = 12;
    //public final String ANAL_METHOD0 = "k5pprbstm20pcts";
    //public final String ANAL_METHOD1 = "k5pprbstm20pctm";
    //public final String ANAL_METHOD0 = "k5pprbstm";
    //public final String ANAL_METHOD1 = "k5pprbstmm";
    public final String ANAL_METHOD0 = "k5pprbstmsm";


    public String getBugOnlySearchString(File f, String buggy_stmt_loc, String search_strategy, String output_dpath) {
	String fpath = f.getPath();
	String bugonly_chunk_loc = "b"+buggy_stmt_loc;
	String rslt = getSearchString(ANAL_METHOD0, "\""+bugonly_chunk_loc+"\"", search_strategy, fpath, output_dpath+"/bugonly_"+search_strategy);
	return rslt;
    }

    public String getLocalContextSearchLoc(ASTNode bug_node, String buggy_stmt_loc) {

	//Load the local context nodes
	SearchChunkGenerator scg = new SearchChunkGenerator();
	SearchChunk lctxt_chunk = scg.getLocalContextChunk(bug_node, MAX_LINES0);
	String lctxt_chunk_loc = null;
	List<SearchNode> lctxt_snode_list = lctxt_chunk.getNodeList();

	//Produce the local context loc string
	boolean is_bug_nested = true;
	for (SearchNode lctxt_snode : lctxt_snode_list) {
	    ASTNode lctxt_node = lctxt_snode.getASTNode();
	    int lctxt_prop = lctxt_snode.getProp();	    
	    if (lctxt_prop == 0) { is_bug_nested = false; }
	    String lctxt_snode_loc = getLocString((CompilationUnit) bug_node.getRoot(), lctxt_node, lctxt_prop);
	    if (lctxt_chunk_loc == null) {
		lctxt_chunk_loc = lctxt_snode_loc;
	    }
	    else {
		lctxt_chunk_loc += ";" + lctxt_snode_loc;
	    }
	}
	if (is_bug_nested) {
	    if (lctxt_chunk_loc != null) {
		String[] buggy_stmt_loc_items = buggy_stmt_loc.split(":");
		lctxt_chunk_loc += ";bslc-nested:" + buggy_stmt_loc_items[1];
	    }
	}
	return lctxt_chunk_loc;
    }
    

    public String getLocalContextSearchString(File f, String buggy_stmt_loc, String search_strategy, String output_dpath) {

	//Load the bug node
	String fpath = f.getPath();
	CompilationUnit cu = (CompilationUnit) ASTNodeLoader.getASTNode(f);
	List<ASTNode> found_nodes = ASTNodeFinder.find(cu, buggy_stmt_loc);
	ASTNode bug_node = found_nodes.get(0);
	
	//Generate search loc
	String lctxt_chunk_loc = getLocalContextSearchLoc(bug_node, buggy_stmt_loc);
	
	//Produce the search string
	String rslt = getSearchString(ANAL_METHOD0, "\""+lctxt_chunk_loc+"\"", search_strategy, fpath, output_dpath+"/lctxt_"+search_strategy);

	return rslt;
    }


    public List<String> getSearchStrings(File f, String buggy_stmt_loc, String output_dpath) {
	List<String> rslt_strs = new ArrayList<String>();
	String rslt_str0 = getBugOnlySearchString(f, buggy_stmt_loc, "even_weighted", output_dpath);
	rslt_strs.add(rslt_str0);
	String rslt_str1 = getLocalContextSearchString(f, buggy_stmt_loc, "bug_weighted", output_dpath);
	rslt_strs.add(rslt_str1);	
	String rslt_str2 = getLocalContextSearchString(f, buggy_stmt_loc, "even_weighted", output_dpath);
	rslt_strs.add(rslt_str2);	
	String rslt_str3 = getLocalContextSearchString(f, buggy_stmt_loc, "ctxt_weighted", output_dpath);
	rslt_strs.add(rslt_str3);
	return rslt_strs;
    }

    public String getSearchString(String anal_method, String data, String strategy, String fpath, String rslt_path) {
	
	String s = "/home/qx5/codesearch/cocker/bin/cockerq";
	s += " -a " + anal_method;
	s += " -n fred4";
	s += " -data " + data;
	s += " -s " + strategy;
	s += " " + fpath;
	if (rslt_path != null) {
	    s += " &> " + rslt_path;
	}
	return s;
    }

    private String getLocString(CompilationUnit cu, ASTNode node, int prop) {
	int start_pos = node.getStartPosition();
	int ln = cu.getLineNumber(start_pos);
	int cn = cu.getColumnNumber(start_pos);
	String rslt = "slc:"+ln+","+cn;
	if (prop == 0) { return "b"+rslt; }
	else if (prop == 1) { return "lc"+rslt; }
	else if (prop == 2) { return "rc"+rslt; }
	else if (prop == 3) { return "gc"+rslt; }
	else { return rslt; }
    }

    private String getMethodLocString(CompilationUnit cu, ASTNode node) {
	ASTNode mnode = node;
	while (mnode != null) {
	    if (mnode instanceof MethodDeclaration) { break; }
	    mnode = mnode.getParent();
	}
	String rslt = "";
	if (mnode != null) {
	    rslt = getLocString(cu, mnode, 1);
	}
	return rslt;
    }

    private String makeNested(String loc) {
	String newloc = null;
	String[] sublocs = loc.split(";");
	for (String subloc : sublocs) {
	    String[] subloc_items = subloc.split(":");
	    if (subloc_items[0].endsWith("-nested")) {
		if (newloc == null) { newloc = subloc; }
		else { newloc += ";" + subloc; }
	    }
	    else {
		String newsubloc = subloc_items[0]+"-nested:"+subloc_items[1];
		if (newloc == null) { newloc = newsubloc; }
		else { newloc += ";" + newsubloc; }
	    }
	}
	return newloc;
    }
}
