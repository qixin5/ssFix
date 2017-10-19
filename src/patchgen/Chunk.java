package edu.brown.cs.ssfix.patchgen;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import edu.brown.cs.ssfix.util.*;

public class Chunk
{
    String fpath;
    String loc;
    List<ASTNode> node_list;
    String fctnt;
    boolean is_normalized;

    /*
    public Chunk(String fpath, String loc) {
	this.fpath = fpath;
	this.loc = loc;
	this.node_list = loadNodes();
	this.is_normalized = false;
    }
    */

    public Chunk(String fpath, String loc, List<ASTNode> node_list, String fctnt) {
	this.fpath = fpath;
	this.loc = loc;
	this.node_list = node_list;
	this.fctnt = fctnt;
	this.is_normalized = false;
    }

    /*
    private List<ASTNode> loadNodes() {
	CompilationUnit cu = null;
	try {
	    cu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(new File(fpath));
	} catch (Throwable t) {
	    System.err.println(t);
	    t.printStackTrace();
	}
	if (cu == null) { return new ArrayList<ASTNode>(); }
	else { return ASTNodeFinder.find(cu, loc); }
    }
    */

    public String getFilePath() { return fpath; }

    public String getLoc() { return loc; }

    public List<ASTNode> getNodeList() { return node_list; }

    public String getFileContent() { return fctnt; }

    public int getLengthInLines() {
	if (node_list==null || node_list.isEmpty()) { return 0; }
	else {
	    int length = 0;
	    CompilationUnit cu = (CompilationUnit) node_list.get(0).getRoot();
	    for (ASTNode node : node_list) {
		int start_pos = node.getStartPosition();
		int end_pos = start_pos + node.getLength();
		length += cu.getLineNumber(end_pos) - cu.getLineNumber(start_pos) + 1;
	    }
	    return length;
	}
    }

    public boolean isNormalized() { return is_normalized; }
    
    public void normalize() {
	if (node_list.isEmpty()) { return; }
	ASTNode md_node = node_list.get(0);
	while (md_node != null) {
	    if (md_node instanceof MethodDeclaration) { break; }
	    else { md_node = md_node.getParent(); }
	}
	if (md_node != null) {
	    Normalizer.normalize(md_node);
	    is_normalized = true;
	}
    }
}
