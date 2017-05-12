package search;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import util.*;


public class SearchFilter
{
    private static ASTNodeStringRegularizer node_str_reg = new ASTNodeStringRegularizer();
    public static void main(String[] args) {
	String fpath = args[0];
	String floc = args[1];
	String rfpath = args[2];

	//Get the buggy code
	File f = new File(fpath);
	ASTNode root_node = ASTNodeLoader.getASTNode(f);
	if (root_node == null) { return; }
	List<ASTNode> nodes = ASTNodeFinder.find((CompilationUnit) root_node, floc);
	String reg_code = "";
	for (ASTNode node : nodes) {
	    reg_code += node_str_reg.getPattern(node);
	    reg_code += " ";
	}
	reg_code = reg_code.trim();

	//Get the filtered result
	File rf = new File(rfpath);
	String filtered_rslt_str = filter(rf, reg_code);
	System.out.println(filtered_rslt_str);
	
    }

    public static String filter(File rslt_f) {
	return filter(rslt_f, null, 1000);
    }

    public static String filter(File rslt_f, int max_size) {
	return filter(rslt_f, null, max_size);
    }
    public static String filter(File rslt_f, String bug_reg_code) {
	return filter(rslt_f, bug_reg_code, 1000);
    }

    public static String filter(File rslt_f, String bug_reg_code, int max_size) {
	List<String> rslt_lines = null;
	try { rslt_lines = FileUtils.readLines(rslt_f, (String) null); }
	catch (Throwable t) { System.err.println(t); t.printStackTrace(); }
	if (rslt_lines == null) { return ""; }
	return filter(rslt_lines, bug_reg_code, max_size);
    }

    public static String filter(List<String> rslt_lines, String bug_reg_code) {
	return filter(rslt_lines, bug_reg_code, 1000);
    }
    
    public static String filter(List<String> rslt_lines, String bug_reg_code, int max_size) {
	StringBuilder sb = new StringBuilder();
	int rslt_lines_size = rslt_lines.size();
	if (rslt_lines_size == 0) { return ""; }
	rslt_lines_size = (rslt_lines_size <= max_size) ? rslt_lines_size : max_size;
	int i1 = 0, i2 = 0;
	String curr_score = null;
	for (int i=0; i<rslt_lines_size; i++) {
	    String rslt_line = rslt_lines.get(i);
	    if (rslt_line == null) { break; }
	    rslt_line = rslt_line.trim();
	    if (rslt_line.equals("")) { break; }

	    String[] elems = rslt_line.split(",");	    
	    String rslt_score = elems[elems.length-1];
	    if (i==0) { curr_score = rslt_score; }
	    else {
		if (rslt_score.equals(curr_score)) {
		    i2 += 1;
		}
		else {
		    String filtered_rslt_str = filter(rslt_lines, i1, i2, bug_reg_code);
		    if (!filtered_rslt_str.equals("")) {
			sb.append(filtered_rslt_str);
			sb.append("\n");
		    }
		    i1 = i2 = i;
		    curr_score = rslt_score;
		}
	    }
	}
	String filtered_rslt_str = filter(rslt_lines, i1, i2, bug_reg_code);
	if (!filtered_rslt_str.equals("")) {
	    sb.append(filtered_rslt_str);
	    sb.append("\n");
	}

	return sb.toString();
    }

    private static String filter(List<String> rslt_lines, int i1, int i2, String bug_reg_code) {
	StringBuilder sb = new StringBuilder();
	Set<String> reg_code_set = new HashSet<String>();
	if ((i1 == 0) && (bug_reg_code != null)) {
	    reg_code_set.add(bug_reg_code);
	}
	//============
	//System.err.println(bug_reg_code);
	//============	
	for (int i=i1; i<=i2; i++) {
	    String rslt_line = rslt_lines.get(i);
	    List<ASTNode> nodes = extractNodes(rslt_line);
	    if (nodes.isEmpty()) { continue; }

	    String reg_code = null;
	    for (ASTNode node : nodes) {
		if (node == null) {
		    reg_code = null;
		    break;
		}
		else {
		    if (reg_code == null) {
			reg_code = node_str_reg.getPattern(node);
		    }
		    else {
			reg_code += node_str_reg.getPattern(node);
		    }
		    reg_code += " ";
		}
	    }
	    if (reg_code == null) { continue; }
	    reg_code = reg_code.trim();
	    //============
	    //System.err.println(reg_code);
	    //============	
	    boolean never_seen = reg_code_set.add(reg_code);
	    //============
	    //System.err.println(never_seen);
	    //============	
	    if (never_seen) {
		sb.append(rslt_line);
		sb.append("\n");
	    }
	}
	return sb.toString().trim();
    }

    private static List<ASTNode> extractNodes(String rslt_line) {
	int i1 = rslt_line.indexOf(",");
	int i2 = rslt_line.lastIndexOf(",");
	String fpath = rslt_line.substring(0, i1);
	String loc = rslt_line.substring(i1+1, i2);
	fpath = fpath.substring(7); //Omit "files://"
	//String fpath_items = fpath.split(":");
	//for (String fpath_item : fpath_items) {
	//    if (fpath.endsWith(".jar")) { //Unreachable fpath
	//	return new ArrayList<ASTNode>();
	//    }
	//}
	File f = new File(fpath);
	if (!f.exists() || !f.canRead()) { return new ArrayList<ASTNode>(); }
	ASTNode root_node = ASTNodeLoader.getASTNode(f);
	if (root_node == null) { return new ArrayList<ASTNode>(); }
	else { return ASTNodeFinder.find((CompilationUnit) root_node, loc); }
    }
}
