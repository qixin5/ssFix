package edu.brown.cs.ssfix.repair;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.apache.commons.io.FileUtils;
import edu.brown.cs.ssfix.util.*;


public class FaultFetcher
{
    private static boolean USE_STACKTRACE = true;
    
    public static List<PathLoc> fetch(String projsrcdpath, List<String> fauloc_lines, int max_faulty_lines) {
	List<PathLoc> path_loc_list = new ArrayList<PathLoc>();
	int fault_count = 0;
	int fauloc_lines_size = fauloc_lines.size();
	for (int i=0; i<fauloc_lines_size; i++) {
	    String fauloc_line = fauloc_lines.get(i).trim();
	    String bfpath = null, bstmt_loc = null;
	    if (fauloc_line.startsWith("Suspicious line:")) {
		int i0 = fauloc_line.indexOf(":");
		String[] fauloc_line_items = fauloc_line.substring(i0+1).split(",");
		String class_name = fauloc_line_items[0];
		int class_name_i0 = class_name.indexOf("$");
		if (class_name_i0 != -1) {
		    class_name = class_name.substring(0, class_name_i0);
		}
		bfpath = projsrcdpath+"/"+class_name.replace(".", "/")+".java";
		File bf = new File(bfpath);
		if (!bf.exists()) {
		    System.err.println("Skip \""+fauloc_line+"\"");
		    continue;
		}
		String ln_str = fauloc_line_items[1];
		int ln = Integer.parseInt(ln_str);
		bstmt_loc = getStmtLocByLineNumber(bfpath, ln);
	    }
	    else if (USE_STACKTRACE && fauloc_line.startsWith("at ")) {
		int i0 = fauloc_line.indexOf("(");
		int i1 = fauloc_line.indexOf(")");
		if (i0 == -1 || i1 == -1) {
		    System.err.println("Unrecognized fauloc line\""+fauloc_line+"\"");
		    continue;
		}
		String tmp0 = fauloc_line.substring(3, i0);
		String class_name = tmp0.substring(0, tmp0.lastIndexOf(".")); //remove the method name
		int class_name_i0 = class_name.indexOf("$");
		if (class_name_i0 != -1) {
		    class_name = class_name.substring(0, class_name_i0);
		}
		bfpath = projsrcdpath+"/"+class_name.replace(".", "/")+".java";
		File bf = new File(bfpath);
		if (!bf.exists()) {
		    System.err.println("Skip \""+fauloc_line+"\"");
		    continue;
		}
		String tmp1 = fauloc_line.substring(i0+1, i1);
		int i2 = tmp1.indexOf(":");
		if (i2 != -1) {
		    String fname = tmp1.substring(0, i2);
		    if (!bfpath.endsWith(fname)) {
			System.err.println("Unrecognized fauloc line\""+fauloc_line+"\"");
			continue;
		    }
		    else {
			String ln_str = tmp1.substring(i2+1);
			int ln = Integer.parseInt(ln_str);
			bstmt_loc = getStmtLocByLineNumber(bfpath, ln);
		    }
		}
		else {
		    System.err.println("Unrecgonized fauloc line\""+fauloc_line+"\"");
		    continue;
		}
	    }

	    if (bfpath != null && bstmt_loc != null) {
		fault_count += 1;
		if (fault_count >= max_faulty_lines) { break; }
		PathLoc path_loc = new PathLoc(bfpath, bstmt_loc);
		path_loc_list.add(path_loc);
	    }
	}
	
	return path_loc_list;
    }

    private static String getStmtLocByLineNumber(String bfpath, int ln) {
	File bf = new File(bfpath);
	List<String> bf_lines = null;
	try { bf_lines = FileUtils.readLines(bf, (String)null); }
	catch (Throwable t) { System.err.println(t); t.printStackTrace(); }
	if (bf_lines == null) { return null; }

	String target_bf_line = bf_lines.get(ln-1);
	char[] char_arr = target_bf_line.toCharArray();
	int length = char_arr.length;
	int cn = -1;
	for (int i=0; i<length; i++) {
	    if (!Character.isWhitespace(char_arr[i])) {
		cn = i;
		break;
	    }
	}
	if (cn == -1) { return null; }
	
	CompilationUnit cu = (CompilationUnit) ASTNodeLoader.getASTNode(bf);
	List<ASTNode> found_nodes = ASTNodeFinder.find(cu, "slc:"+ln+","+cn);
	if (found_nodes.isEmpty() || found_nodes.get(0)==null) { return null; }

	ASTNode found_node = found_nodes.get(0);
	if (found_node instanceof Statement) {
	    int found_node_pos = found_node.getStartPosition();
	    return "slc:"+cu.getLineNumber(found_node_pos)+","+cu.getColumnNumber(found_node_pos);
	}
	else {
	    ASTNode curr_node = found_node;
	    while (curr_node != null) {
		if (curr_node instanceof Statement) { break; }
		else { curr_node = curr_node.getParent(); }
	    }
	    if (curr_node != null) {
		int curr_node_pos = curr_node.getStartPosition();
		return "slc:"+cu.getLineNumber(curr_node_pos)+","+cu.getColumnNumber(curr_node_pos);
	    }
	}

	return null;
    }
    
}
